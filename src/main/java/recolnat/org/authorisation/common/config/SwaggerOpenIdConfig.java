package recolnat.org.authorisation.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition
@Slf4j
public class SwaggerOpenIdConfig {

	/**
	 * define in swagger
	 */
    private static final String OPEN_ID_SCHEME_NAME = "authorisation-oidc";
    private static final String OPENID_CONFIG_FORMAT = "%s/.well-known/openid-configuration";
    private static final String OPENID_CONNECT_CONFIG_FORMAT = "%s/protocol/openid-connect";


    @Bean
    OpenAPI customOpenApi(AuthProperties keycloakProperties) {
    	log.trace(keycloakProperties.toString());
    	   OpenAPI openAPI = new OpenAPI();
    	   openAPI.setInfo(getInfo());
        return openAPI
                .components(new Components()
                        .addSecuritySchemes(OPEN_ID_SCHEME_NAME, createOpenIdSchemeoauth(keycloakProperties)))
                .addSecurityItem(new SecurityRequirement().addList(OPEN_ID_SCHEME_NAME));
    }

    /**
     * affiche la liste complete des openid-configuration definis dans keycloak
     * déclaration de type OIDC
     * @param properties
     * @return
     */
    @SuppressWarnings("unused")
	private SecurityScheme createOpenIdScheme(AuthProperties properties) {
        String connectUrl = String.format(OPENID_CONFIG_FORMAT, properties.getAuthServerUrlOpenApi());

        return new SecurityScheme()
                .type(SecurityScheme.Type.OPENIDCONNECT)
                .openIdConnectUrl(connectUrl);
    }
    
    /**
     * declaration de type Implicit
     * @param properties
     * @return
     */
    private SecurityScheme createOpenIdSchemeoauth(AuthProperties properties) {
    	
  
        String connectUrl = String.format(OPENID_CONNECT_CONFIG_FORMAT, properties.getAuthServerUrlOpenApi());

        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2).description("Oauth2 Flow")
                .flows(new OAuthFlows().implicit(
                		new OAuthFlow().authorizationUrl(connectUrl.concat("/auth"))
                		.tokenUrl(connectUrl.concat("/token")).scopes(new Scopes())));
    }
    
  
    
    private Info getInfo() {
        var info = new Info();
        info.setDescription("""
        		Authorisation API. Le réseau national des collections naturalistes 
        		est une infrastructure de recherche française dont l'objectif est 
        		de produire un corpus de données basé sur les collections d'histoire naturelle de France, 
        		permettant de valoriser les recherches au service de l'étude de la géo- et de la biodiversité actuelle et passée.
        		Liens utiles:
        		- [RECOLNAT](https://fr.wikipedia.org/wiki/RECOLNAT)""");
        info.setVersion("1.0");
        info.setTermsOfService("http://swagger.io/terms/");
        info.setLicense(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"));
        info.setContact(new Contact().email("apiteam@mnhn.fr"));
       return info;
    }
    
}
