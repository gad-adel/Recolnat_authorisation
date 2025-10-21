package recolnat.org.authorisation.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class AuthProperties {

    private String serverUrl;
    private String realm;
    private String username;
    private String password;
    private String clientId;
    private String appRealm;
    private String authServerUrlOpenApi;
}
