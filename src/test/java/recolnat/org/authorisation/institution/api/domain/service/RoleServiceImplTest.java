package recolnat.org.authorisation.institution.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import recolnat.org.authorisation.AbstractServiceTest;
import recolnat.org.authorisation.api.domain.Institution;
import recolnat.org.authorisation.api.domain.PermissionInfo;
import recolnat.org.authorisation.api.domain.Role;
import recolnat.org.authorisation.api.domain.UserProfile;
import recolnat.org.authorisation.api.domain.enums.RoleEnum;
import recolnat.org.authorisation.connector.api.CollectionApiClient;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;
import recolnat.org.authorisation.connector.api.domain.OutputCollection;
import recolnat.org.authorisation.service.RoleService;
import recolnat.org.authorisation.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/***
 * test dans base postgresprofile= int (integration)
 */
@Slf4j
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@ActiveProfiles("int")
class RoleServiceImplTest extends AbstractServiceTest {
    @Autowired
    private RoleService roleService;


    @MockBean
    private UserService userService;
    @MockBean
    private CollectionApiClient collectionApiClient;

    @Autowired
    private KeycloakAdminClient adminClient;

    @BeforeAll
    static void setup() {
        keycloakContainer.start();
    }


    @Test
    void retrieveAllRole_should_be_ok() {
        UUID uidRespInst = UUID.fromString("b5762fe5-1c8b-438a-8bcb-7d9a877af46d");

        when(userService.getCurrentUser()).thenReturn(UserProfile.builder().uid(uidRespInst)
                .role(Role.builder().id("9ecba787-ea29-4260-b6ce-35dea4d75402")
                        .name("ADMIN_INSTITUTION").code(RoleEnum.ADMIN_INSTITUTION)
                        .description("Responsable d'institution").build()).build());

        var expectedRoles = Arrays.stream(RoleEnum.values()).map(RoleEnum::name).toList();
        var roles = roleService.retrieveAllRole().stream().map(Role::getCode).map(Enum::name).toList();
        assertThat(roles).hasSizeLessThanOrEqualTo(expectedRoles.size());
        assertThat(roles).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedRoles);
        log.info("Result  roleService.retrieveAllRole() : {} ", roles);

    }

    @Test
    void removeUserPermission_should_be_ok() {
        UUID adminColId = UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1");
        UUID uidRespInst = UUID.fromString("b5762fe5-1c8b-438a-8bcb-7d9a877af46d");
        // as resp
        when(userService.getCurrentUser()).thenReturn(UserProfile.builder().uid(uidRespInst)
                .username("admintest1")
                .role(Role.builder().id("9ecba787-ea29-4260-b6ce-35dea4d75402")
                        .name("ADMIN_INSTITUTION").code(RoleEnum.ADMIN_INSTITUTION)
                        .description("Responsable d'institution").build()).institution(Institution.builder().id(1).build()).build());

        var roleAdminCollection = RoleEnum.ADMIN_COLLECTION;
        String roleId = "f738df1a-cc18-4a5c-82d0-4c7388c5031f";

        final var adminUser = UserProfile.builder().uid(adminColId)
                .institution(Institution.builder().id(1).code("MNHN").build())
                .role(Role.builder().id(roleId)
                        .name("ADMIN_COLLECTION").code(roleAdminCollection)
                        .description("Responsable de collection").build()).build();

        when(userService.getUserByUid(adminColId)).thenReturn(adminUser);

        roleService.removeUserPermission(adminColId);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(adminColId).isNotNull();
            softAssertions.assertThat(adminClient.removeRole(adminColId.toString(), roleId)).satisfies();
        });

    }


    @Test
    void addUserPermission_should_be_ok() {
        var expectedRoles = Arrays.stream(RoleEnum.values()).map(RoleEnum::name).toList();
        assertEquals(6, expectedRoles.size());
        UUID uidRespInst = UUID.fromString("b5762fe5-1c8b-438a-8bcb-7d9a877af46d");
        // as resp
        when(userService.getCurrentUser()).thenReturn(UserProfile.builder()
                .uid(uidRespInst)
                .role(Role.builder().id("9ecba787-ea29-4260-b6ce-35dea4d75402")
                        .name(RoleEnum.ADMIN_INSTITUTION.name())
                        .code(RoleEnum.ADMIN_INSTITUTION)
                        .description("Responsable d'institution")
                        .build()).institution(Institution.builder().id(1).institutionId(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                        .build()).build());
        // as resp
        UserProfile assignee = UserProfile.builder()
                .uid(uidRespInst).username("testuser")
                .role(
                        Role.builder()
                                .id("9ecba787-ea29-4260-b6ce-35dea4d75402")
                                .name(RoleEnum.ADMIN_COLLECTION.name())
                                .code(RoleEnum.ADMIN_COLLECTION)
                                .description("Responsable de collection")
                                .build())
                .institution(Institution.builder().id(1).institutionId(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64")).build())
                .build();
        when(userService.getUserByUid(
                UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .thenReturn(
                        assignee);

        when(collectionApiClient.retrieveCollectionsByInstitutions(1))
                .thenReturn(List.of(
                        OutputCollection.builder()
                                .id(UUID.fromString("beb81bac-9515-4415-aa20-5cbc533754fe"))
                                .build(),
                        OutputCollection.builder()
                                .id(UUID.fromString("e828852c-e97f-4f67-a027-12f7018a77e2"))
                                .build()));
        // action

        roleService.addUserPermission(
                UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1"),
                PermissionInfo.builder()
                        .roleId(UUID.fromString("f738df1a-cc18-4a5c-82d0-4c7388c5031f"))
                        .institutionId(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                        .collections(
                                List.of(
                                        UUID.fromString("beb81bac-9515-4415-aa20-5cbc533754fe")))
                        .build());

        assertThat(assignee.getRole()).isNotNull();
        assertThat(assignee.getRole().getCode()).isEqualTo(RoleEnum.ADMIN_COLLECTION);

    }


}
