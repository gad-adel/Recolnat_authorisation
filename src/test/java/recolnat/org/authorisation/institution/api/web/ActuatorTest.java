package recolnat.org.authorisation.institution.api.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import recolnat.org.authorisation.AbstractServiceTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/***
 * test dans base postgresprofile= int (integration)
 */
@ActiveProfiles("int")
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActuatorTest extends AbstractServiceTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void get_actuator() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/actuator")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

}
