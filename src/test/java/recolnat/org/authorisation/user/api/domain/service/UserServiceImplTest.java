package recolnat.org.authorisation.user.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import recolnat.org.authorisation.AbstractServiceTest;
import recolnat.org.authorisation.api.domain.ConnectedUser;
import recolnat.org.authorisation.service.AuthenticationService;
import recolnat.org.authorisation.service.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ActiveProfiles("int")
@Slf4j
class UserServiceImplTest extends AbstractServiceTest {
    @Autowired
    UserService userService;
    @MockBean
    private AuthenticationService authenticationService;

    @BeforeAll
    protected static void setup() {
        keycloakContainer.start();
    }

    @Test
    void getUserByUid() {
        var expectedUserId = UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1");
        var user = userService.getUserByUid(expectedUserId);
        log.info("User found : {}", user);
        assertThat(user).isNotNull();
        assertThat(user.getUid()).isEqualTo(expectedUserId);
    }

    @Test
    void get_all_User_by_page0_size10_ok() {
        int page = 0;
        int size = 10;
        when(authenticationService.getConnected()).thenReturn(ConnectedUser.builder()
                .userId(UUID.fromString(RESP_INST1_UID)).build());
        var userProfilePage = userService.findAll(page, size, "", null);
        assertThat(userProfilePage.getUsers()).hasSizeLessThanOrEqualTo(4);
        assertThat(userProfilePage.getTotalPages()).isLessThanOrEqualTo(4);

    }
}
