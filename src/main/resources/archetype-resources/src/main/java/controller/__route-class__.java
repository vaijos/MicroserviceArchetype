package ${package}.controller;

import static spark.Spark.*;
import spark.Request;
import spark.Response;

import java.nio.file.Files;
import java.nio.file.Paths;
import ${package}.App;

import com.dowjones.mshared.core.errormapper.model.ErrorMsg;
import com.dowjones.mshared.core.http.HttpRespMessage;
import com.dowjones.mshared.core.utils.JsonUtil;
import com.dowjones.mshared.core.utils.MicroServiceUtil;
import com.dowjones.mshared.core.log.LogMessage;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ${route-class} {
    private static final Logger Log = LoggerFactory.getLogger(${route-class}.class);
    private List<String> noDebugRoutes;

    public ${route-class}() {
        noDebugRoutes = new ArrayList<String>();
		noDebugRoutes.add("/swagger");
		noDebugRoutes.add("/swagger_ui");
		noDebugRoutes.add("/about");
		noDebugRoutes.add("/healthcheck");

        get("/swagger", (req, res) -> {
			res.type("application/json");
        	String swaggerJson = new String(Files.readAllBytes(Paths.get(App.getSwaggerfile()))); //"swagger.json"
            return swaggerJson;
        });

        get("/swagger_ui", (req, res) -> {
            res.redirect("swagger-ui/index.html");
			return null;
        });

        get("/about", (req, res) -> MicroServiceUtil.getAbout(App.getProperties(), new ArrayList<String>(Arrays.asList("JKS_PASSWD"))), JsonUtil::toJson);

        get("/healthcheck", (req, res) -> getHealth(), JsonUtil::toJson);

        before((req, res) -> {
			generateInputLog(req);
		});

		after((req, res) -> {
			res.type("application/json");
			generateOutputLog(req, res);
		});

        notFound((req, res) -> handleDefault(req, res));

		exception(IllegalArgumentException.class, (iae, req, res) -> {
			res.status(HttpRespMessage.StatusCode.BAD_REQUEST.getStatusCode()); // 400
			ErrorMsg error = new ErrorMsg(ErrorMsg.ErrorCode.MISSING_OR_INVALID_PARAM.toString(), iae.getMessage(),
					iae.toString(), HttpRespMessage.StatusCode.BAD_REQUEST.getStatusCode(), "");
			res.body(JsonUtil.toJson(error));
			Log.error(LogMessage.getInstance().generateMessage(error));
		});

		exception(Exception.class, (e, req, res) -> {
			res.status(HttpRespMessage.StatusCode.BAD_REQUEST.getStatusCode()); // 400
			ErrorMsg error = new ErrorMsg("", e.getMessage(), e.toString(),
					HttpRespMessage.StatusCode.INTERNAL_ERROR.getStatusCode(), "");
			res.body(JsonUtil.toJson(error));
			Log.error(LogMessage.getInstance().generateMessage(error));
		});
    }

    private String handleDefault(Request req, Response res) {
        res.status(HttpRespMessage.StatusCode.BAD_REQUEST.getStatusCode()); // 400
        return JsonUtil.toJson(new ErrorMsg("", "Resource Not Found", "Resource Not Found",
                HttpRespMessage.StatusCode.RESOURCE_NOT_FOUND.getStatusCode(), "")) ;
    }

    private Map<String, String> getHealth() {
		Map<String, String> health = new HashMap<String, String>();
		health.put("status", "OK");
		// health.put("connection", "OK");
		// health.put("database", "OK");
		return health;
    }

    private void generateInputLog(Request req) {
		if (!noDebugRoutes.contains(req.uri())) {
			Set<String> headersSet = req.headers();
			String headers = "\n";
			for (String header : headersSet) {
				headers += header + ": " + req.headers(header) + "\n";
			}
			String message = "\n++++++++ INPUT ++++++++\n" + req.requestMethod() + " " + req.uri() + "\nHeaders:"
					+ headers + "Body:\n" + req.body() + "\n++++++++ END INPUT ++++++++";
			Log.info(LogMessage.getInstance().generateMessage(message));
		}
	}

	private void generateOutputLog(Request req, Response res) {
		if (!noDebugRoutes.contains(req.uri())) {
			String message = "\n++++++++ OUTPUT ++++++++\n" + req.requestMethod() + " " + req.uri() + "\nStatus: "
					+ res.status() + "\nContent Type: " + res.type() + "\nBody:\n" + res.body()
					+ "\n++++++++ END OUTPUT ++++++++";
			Log.info(LogMessage.getInstance().generateMessage(message));
		}
	}
    
    private String getSwaggerUi(String scheme, String host) {
		return scheme + "://" + host + "/swagger-ui/index.html";
	}
}
