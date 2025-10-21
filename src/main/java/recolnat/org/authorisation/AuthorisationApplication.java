package recolnat.org.authorisation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "recolnat.org.authorisation.common.config")
public class AuthorisationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorisationApplication.class, args);
	}

}
