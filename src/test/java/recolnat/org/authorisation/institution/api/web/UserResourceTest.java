package recolnat.org.authorisation.institution.api.web;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import recolnat.org.authorisation.AbstractServiceTest;
import recolnat.org.authorisation.api.domain.ConnectedUser;
import recolnat.org.authorisation.service.AuthenticationService;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/***
 * test dans base postgresprofile= int (integration)
 */
@ActiveProfiles("int")
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserResourceTest extends AbstractServiceTest {
    public static final String RESP_INST1_UID = "b5762fe5-1c8b-438a-8bcb-7d9a877af46d";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthenticationService authenticationService;

    @BeforeAll
    protected static void setup() {
        keycloakContainer.start();
    }

    @Test
    void getUsers() throws Exception {
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(RESP_INST1_UID)).build());
        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/users?page=0&size=10&institution_id=50f4978a-da62-4fde-8f38-5003bd43ff64")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").exists())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].institutionName", hasToString("Muséum National d'Histoire Naturelle")))
                .andExpect(jsonPath("$.users[0].email", hasToString("respcolltest1@recolnat.com")))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getUserByIdNotAdmin() throws Exception {
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(RESP_INST1_UID)).build());
        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/users/82e20227-b0d7-46b4-b44d-2257d86f67b1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid", hasToString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .andExpect(jsonPath("$.email", hasToString("respcolltest1@recolnat.com")))
                .andExpect(jsonPath("$.username", hasToString("respcolltest1")))
                .andExpect(jsonPath("$.lastName", hasToString("Responsible of collection Test1")))
                .andExpect(jsonPath("$.institution.institutionId", hasToString("50f4978a-da62-4fde-8f38-5003bd43ff64")));

    }

    @Test
    void getUserById() throws Exception {
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(RESP_INST1_UID)).build());
        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/users/712215d4-c795-11ec-9d64-0242ac120002")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid", hasToString("712215d4-c795-11ec-9d64-0242ac120002")))
                .andExpect(jsonPath("$.role.name", hasToString("ADMIN")))
                .andExpect(jsonPath("$.email", hasToString("admintest12@recolnat.com")))
                .andExpect(jsonPath("$.username", hasToString("admintest1")))
                .andExpect(jsonPath("$.lastName", hasToString("Responsible AdminTest1 ")))
                .andExpect(jsonPath("$.role.description", hasToString("Administrateur")));
    }

}
