package recolnat.org.authorisation.connector.api.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import recolnat.org.authorisation.AbstractServiceTest;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;


/**
 * test de chargement du fichier recolnat-oauth2-realm.json dans un  keycloak ( test container)
 */
@SpringBootTest
@ActiveProfiles("int")
@Slf4j
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class KeycloakAdminClientImplTest extends AbstractServiceTest {


    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    @BeforeAll
    protected static void setup() {
        keycloakContainer.start();
    }

    @Test
    void getUsers() {
        var expectedUsers = List.of("admintest1", "respcolltest1", "respinsttest1", "respinsttest2");
        List<UserRepresentation> users = keycloakAdminClient.getUsers();
        List<String> actualUsers = users.stream().map(UserRepresentation::getUsername).toList();
        log.info("Actual Users ========> :" + actualUsers);

        assertThat(actualUsers).hasSize(4);
        assertThat(actualUsers).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedUsers);
    }

    @Test
    void getUserById_should_be_ok() {
        String expectedUid = "b5762fe5-1c8b-438a-8bcb-7d9a877af46d";
        var actual = keycloakAdminClient.getUserById(expectedUid);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(expectedUid);

    }

    @Test
    void getRoleById_should_be_ok() {
        String expectedUid = "311a8089-6ce3-44b4-83cb-55edb2b24a86";//ADMIN_INSTITUTION
        var actual = keycloakAdminClient.getRoleById(expectedUid);
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(expectedUid);

    }

    @Test
    void getRoleByUserId_should_be_ok() {
        String userId = "712215d4-c795-11ec-9d64-0242ac120002";
        var expectedRoles = List.of("ADMIN");
        var actualRoles = keycloakAdminClient.getRoleByUserId(userId).stream().map(RoleRepresentation::getName).toList();
        assertThat(actualRoles).hasSizeLessThanOrEqualTo(1);
        assertThat(actualRoles).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedRoles);

    }


}
