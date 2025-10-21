package recolnat.org.authorisation;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/***
 * test dans base postgres profile= int (integration)
 */
@SpringBootTest
@ActiveProfiles("int")
@Testcontainers
@Slf4j
public abstract class AbstractServiceTest {

    public static final String RESP_INST1_UID = "b5762fe5-1c8b-438a-8bcb-7d9a877af46d";
    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak";
    private static final String KEYCLOAK_VERSION = "23.0.1";
    /**
     * initialise un testcontainer keycloak avec base H2 (default)  avec ActiveProfiles("int")== application-int.yaml
     */
    protected static final KeycloakContainer keycloakContainer = new KeycloakContainer(KEYCLOAK_IMAGE + ":" + KEYCLOAK_VERSION)
            .withRealmImportFile("keycloak/recolnat-oauth2-realm.json")
            .withAdminUsername("admin").withAdminPassword("admin")
            .withEnv("IMP_AUTHORISATION_OPENAPI", "http://localhost:8081/*")
            .withEnv("IMP_EXPLORER_OPENAPI", "http://localhost:8082/*")
            .withEnv("IMP_COLLMANAGER_OPENAPI", "http://localhost:8080/*")
            .withEnv("IMP_DATAWARE_OPENAPI", "http://localhost:8085/*")
            .withEnv("IMP_FRONT", "http://localhost:3000/*")
            .withEnv("KC_HEALTH_ENABLED", "true")
            .withEnv("KC_HTTP_ENABLED", "true")
            .withEnv("KC_METRICS_ENABLED", "true")
            .withEnv("SECRET_DATAWARE", "N1LSVQr5JPsI1OVQl51GKOnAkxR51z7O")
            .withEnv("SECRET_COLLECTION_MANAGER", "ee5jLU9Z27S1SNyusPI5o8ODmcjJ0Mw5")
            .withEnv("SECRET_AUTHORIZATION", "eaCcFXA78r1eYmmDnZPh8jv42e7wwabU")//.withEnv("KC_DB","H2")
            .withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES)).withReuse(true);
    private static final String BASE_IMAGE = "postgres";
    private static final String BASE_VERSION = "16.0-bullseye";
    /**
     * initialise un testcontainer postgres pour environnement avec ActiveProfiles("int")== application-int.yaml
     */
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(BASE_IMAGE + ":" + BASE_VERSION)
            .withDatabaseName("rcn")
            .withPassword("datawh")
            .withUsername("datawh")
            .withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("postgres/init_postgres_instance.sql")
                    , "/docker-entrypoint-initdb.d/").withReuse(true);

    @DynamicPropertySource
    protected static void dynamicProperties(DynamicPropertyRegistry registry) {
        if (keycloakContainer.isRunning()) {
            registry.add("auth.server-url", keycloakContainer::getAuthServerUrl);
            registry.add("auth.realm", () -> "master");
            registry.add("auth.username", () -> "admin");
            registry.add("auth.password", () -> "admin");
            registry.add("auth.clientId", () -> "admin-cli");
            registry.add("auth.app-realm", () -> "recolnat-oauth2");
            registry.add("auth.realmOpenApi", () -> "recolnat-oauth2");
            registry.add("auth.auth-server-url-OpenApi", keycloakContainer::getAuthServerUrl);

            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                    () -> keycloakContainer.getAuthServerUrl() + "/realms/recolnat-oauth2");
        }
    }

    @AfterAll
    static void afterAll() {
        keycloakContainer.stop();
        postgreSQLContainer.stop();
    }

}
