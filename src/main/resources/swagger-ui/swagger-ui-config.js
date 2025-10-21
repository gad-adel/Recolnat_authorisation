window.onload = function () {
    // Begin Swagger UI call region
    const ui = SwaggerUIBundle({
        url: "../specs/authorisation-api-spec.yaml",
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout",
        oauth2RedirectUrl: window.location.protocol + "//" + window.location.hostname + ":" + window.location.port + "/swagger-ui/oauth2-redirect.html",
        withCredentials: true
    })
    // End Swagger UI call region

    ui.initOAuth({
        clientId: "authorisation-api-spec",
        scopes: "email profile",
        usePkceWithAuthorizationCodeGrant: true
    });

    window.ui = ui;
}
