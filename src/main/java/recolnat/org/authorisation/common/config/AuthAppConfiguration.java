package recolnat.org.authorisation.common.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Provides a Keycloak client builder with the ability to customize
 *  the underlying RESTEasy client used to communicate with the Keycloak server. 
 */
@Configuration
@EnableConfigurationProperties
@RequiredArgsConstructor
public class AuthAppConfiguration {

   private final AuthProperties authProperties;

    @Bean
    Keycloak keycloak(){
    return KeycloakBuilder.builder()
        .serverUrl(authProperties.getServerUrl())
        .realm(authProperties.getRealm())
        .username(authProperties.getUsername())
        .password(authProperties.getPassword())
        .clientId(authProperties.getClientId())
        .grantType(OAuth2Constants.PASSWORD)
        .build();
    }

}
