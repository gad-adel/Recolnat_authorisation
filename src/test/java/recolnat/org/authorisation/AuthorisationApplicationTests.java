package recolnat.org.authorisation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *  test env spring with int profile
 */
@SpringBootTest
@ActiveProfiles("int")
class AuthorisationApplicationTests {
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertThat(Arrays.stream(applicationContext.getBeanDefinitionNames()).allMatch(Objects::nonNull)).isTrue();

	}

}
