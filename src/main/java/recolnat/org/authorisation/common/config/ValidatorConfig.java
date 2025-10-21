package recolnat.org.authorisation.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

@Configuration
public class ValidatorConfig {

	@Bean
	 Validator validator() {
		var buildDefaultValidatorFactory = Validation.buildDefaultValidatorFactory();
		return buildDefaultValidatorFactory.getValidator();
	}
}
