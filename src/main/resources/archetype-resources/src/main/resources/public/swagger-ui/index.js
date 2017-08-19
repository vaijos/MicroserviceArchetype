$(function () {
    var port = window.location.port;
    if(port.length) {
        port = ":" + port;
    } 
    var url = window.location.protocol + "//" + window.location.hostname + port + "/swagger";

    window.swaggerUi = new SwaggerUi({
        url: url,
        dom_id: "swagger-ui-container",
        validatorUrl: null,
        supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch'],
        onComplete: function(swaggerApi, swaggerUi){
            if(typeof initOAuth == "function") {
                initOAuth({
                    clientId: "your-client-id",
                    clientSecret: "your-client-secret-if-required",
                    realm: "your-realms",
                    appName: "your-app-name", 
                    scopeSeparator: ",",
                    additionalQueryStringParams: {}
                });
            }
        },
        docExpansion: "none",
        jsonEditor: false,
        apisSorter: "alpha",
        defaultModelRendering: 'schema',
        showRequestHeaders: false
    });

    function addApiKeyAuthorization(){
        var key = encodeURIComponent($('#input_apiKey')[0].value);
        if(key && key.trim() != "") {
            var apiKeyAuth = new SwaggerClient.ApiKeyAuthorization("api_key", key, "query");
            window.swaggerUi.api.clientAuthorizations.add("api_key", apiKeyAuth);
        }
    }

    $('#input_apiKey').change(addApiKeyAuthorization);

    window.swaggerUi.load();
});