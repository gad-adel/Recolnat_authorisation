package recolnat.org.authorisation.institution.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.recolnat.model.PermissionRequestDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import recolnat.org.authorisation.AbstractServiceTest;
import recolnat.org.authorisation.api.domain.ConnectedUser;
import recolnat.org.authorisation.service.AuthenticationService;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/***
 * test dans base postgresprofile= int (integration)
 */
@ActiveProfiles("int")
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleResourceTest extends AbstractServiceTest {
    public static final String ADMIN_UID = "712215d4-c795-11ec-9d64-0242ac120002";
    public static final String ADMIN_INSTITUTION_UID = "b5762fe5-1c8b-438a-8bcb-7d9a877af46d";
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private WebApplicationContext context;

    @BeforeAll
    protected static void setup() {
        keycloakContainer.start();
    }

    @BeforeEach
    public void init() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                // will perform all of the initial setup we need to integrate Spring Security with Spring MVC Test
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser()
    void getRoles() throws Exception {
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(RESP_INST1_UID)).build());
        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/roles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$", hasSize(6)));
    }


    @Test
    void getRoles_in_anonymous_should_be_ko() throws Exception {
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(RESP_INST1_UID)).build());
        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/roles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void remove_permission_admin_inst_should_be_ok() throws Exception {
        var uid = "8674ebbe-83ae-4fff-aa22-be59675e80b9";
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(ADMIN_UID)).build());

        mvc.perform(
                        delete("/v1/users/{uid}/permissions", uid)
                                .with(opaqueToken().attributes(att -> att.put("sub", ADMIN_UID))
                                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("uid", equalTo(uid)));
    }


    /**
     * scenario: le user est responsable de collection et devient user_infra
     * 1. suppression de permission par l'ADMIN_INSTITUTION qui seul a le droit de le faire
     * 2. ajout de permission par l'ADMIN qui seul a le droit de le faire
     *
     */
    @Test
    void addPermission_infra_should_be_ok() throws Exception {
        var uidRespColl = "82e20227-b0d7-46b4-b44d-2257d86f67b1";
        UUID roleInfra = UUID.fromString("47b2e909-e553-49fe-ab84-39ab2306a1f5");
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(ADMIN_INSTITUTION_UID)).build());

        mvc.perform(
                        delete("/v1/users/{uid}/permissions", uidRespColl)
                                .with(opaqueToken().attributes(att -> att.put("sub", ADMIN_INSTITUTION_UID))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(ADMIN_UID)).build());
        mvc.perform(
                        put("/v1/users/{uid}/permissions", uidRespColl)
                                .with(opaqueToken().attributes(att -> att.put("sub", ADMIN_UID))
                                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new PermissionRequestDTO().roleId(roleInfra))))
                .andExpect(status().isOk());
    }

    /**
     * Imcompatible of pair roles  :ADMIN_INSTITUTION avec ADMIN,ADMIN_INSTITUTION,USER_INFRA
     */
    @ParameterizedTest
    @ValueSource(strings = {"b9a746ef-3dd6-4954-8e14-a317e380524e",
            "311a8089-6ce3-44b4-83cb-55edb2b24a86", "47b2e909-e553-49fe-ab84-39ab2306a1f5"})
    void add_not_auth_permission_as_admin_institution_should_be_ko(String roleId) throws Exception {
        var uid = ADMIN_INSTITUTION_UID;
        UUID otherRole = UUID.fromString(roleId);
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(ADMIN_INSTITUTION_UID)).build());

        mvc.perform(
                        put("/v1/users/{uid}/permissions", uid)
                                .with(opaqueToken().attributes(att -> att.put("sub", uid))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new PermissionRequestDTO()
                                                .roleId(otherRole)
                                                .institutionId(new UUID(0, 1)))))
                .andExpect(status().isForbidden());
    }


    /**
     * Imcompatible of pair roles  :ADMIN avec ADMIN,ADMIN_COLLECTION,DATA_ENTRY
     */
    @ParameterizedTest
    @ValueSource(strings = {"b9a746ef-3dd6-4954-8e14-a317e380524e",
            "f738df1a-cc18-4a5c-82d0-4c7388c5031f", "6dbed320-03b8-4df1-834c-e74e649dc74c"})
    void add_not_auth_permission_as_admin_to_myself_should_be_ko(String roleId) throws Exception {
        var uid = ADMIN_UID;
        UUID otherRole = UUID.fromString(roleId);
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(ADMIN_UID)).build());

        mvc.perform(
                        put("/v1/users/{uid}/permissions", uid)
                                .with(opaqueToken().attributes(att -> att.put("sub", uid))
                                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new PermissionRequestDTO()
                                                .roleId(otherRole)
                                                .institutionId(new UUID(0, 1)))))
                .andExpect(status().isForbidden());
    }

}
