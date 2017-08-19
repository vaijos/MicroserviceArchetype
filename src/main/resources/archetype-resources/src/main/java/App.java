package ${package};

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Vector;

import ${package}.controller.${route-class};

import com.dowjones.mshared.core.utils.MicroServiceUtil;
import com.dowjones.mshared.core.utils.SwaggerUtil;
import com.dowjones.mshared.core.errormapper.ErrorMapper;
import com.dowjones.mshared.core.http.StaticHttpsChannel;
import com.dowjones.mshared.core.log.LogMessage;

import spark.Spark;
import static spark.Spark.options;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.threadPool;
import static spark.Spark.staticFileLocation;

import java.util.Map;
import java.util.HashMap;

import java.net.InetAddress;

public class App {
    public static final String VERSION = "1.0.1";
    public static final Vector<String> allowedOrigins = new Vector<String>();
	public static final Map<String, String> appVariables = new HashMap<String, String>();

    static final String ERRORMAPPER_FILE = "error_mappings.dat";
    static final String CONFIG_FILE		 = "config.properties";
    static final String SWAGGER_FILE	 = "swagger.json";
    
    private static Properties m_prop = new Properties();
	private static ErrorMapper emapper;
	
	private static final Logger Log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
    	MicroServiceUtil.mapCommandLine(args, appVariables);
		new App().start();
    }

    public void start()
    {
        if (init() == false) {
            Log.error(LogMessage.getInstance().generateMessage("Initialization failed, exiting..."));
            return;
        }
        try {
			m_prop.put("${artifactId}Version", App.VERSION);
			if (appVariables.containsKey("dockerImage")) {
				m_prop.put("DockerImage", appVariables.get("dockerImage"));
			}

			String swaggerVersion = SwaggerUtil.getSwaggerVersion(getSwaggerfile());
			if (swaggerVersion != null) {
				m_prop.put("SwaggerVersion", swaggerVersion);
			}

            // configure default thread pool
            int maxThreads    = Integer.parseInt(getProperty("maxThreads"));
            int minThreads    = Integer.parseInt(getProperty("minThreads"));
            int timeOutMillis = Integer.parseInt(getProperty("timeOutMillis"));
            threadPool(maxThreads, minThreads, timeOutMillis);

			Spark.port(Integer.parseInt(getProperty("SPARK_PORT")));
			String https = getProperty("SPARK_HTTPS");
			if (https != null && https.equalsIgnoreCase("true")) {
				String sparkJKS = String.format("config/%s/%s", getEnv(), getProperty("SPARK_JKS"));
				Spark.secure(sparkJKS, getProperty("JKS_PASSWD"), null, null);
			}

			staticFileLocation("/public");

			// Wildcard (*) allows any domain to make requests to the API
			allowedOrigins.add("*");

			// Inserting domains to the allowedOrigins list allows them to make requests to the API. 
            // Example: allowedOrigins.add("http://localhost:7654");
			enableCORS(allowedOrigins, "*", "*");

            new ${route-class}();
        } catch (Exception e) {
        	Log.error(LogMessage.getInstance().generateMessage(e.getMessage()));
            e.printStackTrace();
        }
        Log.debug(LogMessage.getInstance().generateMessage("Spark service is up"));
    }

    public static boolean init() {
		setEnvironmentFromCommandLine();
		LogMessage.getInstance().init(appVariables.get("env"));
		String hostName = null;
		try {
		    hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			e.printStackTrace();
		}
        m_prop = new Properties();
        String propFileName = getConfigfile(); 
		Log.debug(LogMessage.getInstance().generateMessage("Loading configure file:" + propFileName));
        try {
            m_prop.load(new FileInputStream(propFileName));
            emapper = ErrorMapper.getInstance();
            emapper.loadErrorMappings(getErrormapperfile());
			String certFile = String.format("config/%s/%s", getEnv(), getProperty("HTTPCLNT_CERT"));
			StaticHttpsChannel.init(App.getProperty("CERTS_HOST_MATCH").equalsIgnoreCase("true"), certFile);
            Log.info(LogMessage.getInstance().generateMessage("Initialization File load complete..."));
            return true;
        } catch(Exception e) {
        	Log.error(LogMessage.getInstance().generateMessage(e.getMessage()));
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }
    }

	private static void setEnvironmentFromCommandLine() {
		boolean isEnvOK = false;
		if (appVariables.containsKey("env")) {
			String env = appVariables.get("env");
			String[] targetEnvs = new String[]{ "dev", "int", "stag", "prod" };
			for(int index = 0; index < targetEnvs.length; index++) {
				if (env.equals(targetEnvs[index])) {
					isEnvOK = true;
					break;
				}
			}
			if (!isEnvOK) {
				Log.error(LogMessage.getInstance().generateMessage("Environment " + env + " is invalid. Using dev environemt"));
				App.setEnv("dev");
			}
		} else {
			Log.error(LogMessage.getInstance().generateMessage("Environment not defined. Using dev environment"));
			App.setEnv("dev");
		}
	}

    public static Properties getProperties() {
		return m_prop;
	}

	public static String getProperty(String item) {
        return m_prop.getProperty(item);
    }

    public static String getConfigfile() {
		if(getEnv()!= null) {
			return  "config/" + getEnv() + "/" + CONFIG_FILE;
        }
		return CONFIG_FILE;
	}

    public static void setEnv(String env) {
		appVariables.put("env", env);
	}

	public static String getEnv() {
		return appVariables.get("env");
	}

    public static String getSwaggerfile() {
		if(getEnv() != null) {
			return "config/" + getEnv() + "/" + SWAGGER_FILE;
		}		
		return SWAGGER_FILE;
	}

    public static String getErrormapperfile() {
		if(getEnv()!= null) {
		    return  "config/" + getEnv() + "/" + ERRORMAPPER_FILE;
		}
		return ERRORMAPPER_FILE;
	}

    public static ErrorMapper getEmapper() {
		if(emapper == null) {
		    emapper = ErrorMapper.getInstance();
		}
		return emapper;
	}

	private static void enableCORS(final Vector<String> origins, final String methods, final String headers) {
		options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if(accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}
			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if(accessControlRequestMethod != null) {
			    response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}
			return "OK";
		});

		before((request, response) -> {
			String origin = request.headers("Origin");
			if(origins.contains("*")) {
				response.header("Access-Control-Allow-Origin", "*");
			} else if(origins.contains(origin)) {
				response.header("Access-Control-Allow-Origin", origin);
			}
			response.header("Access-Control-Request-Method", methods);
			response.header("Access-Control-Allow-Headers", headers);
		});
	}
}
