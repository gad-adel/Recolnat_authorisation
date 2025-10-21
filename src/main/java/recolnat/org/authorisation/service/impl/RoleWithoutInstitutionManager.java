package recolnat.org.authorisation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import recolnat.org.authorisation.api.domain.PermissionInfo;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;
import recolnat.org.authorisation.service.RoleManager;
import recolnat.org.authorisation.service.RoleService;

import java.util.HashMap;
import java.util.List;

import static recolnat.org.authorisation.service.impl.RoleServiceImpl.DESCRIPTION_ROLE;


@Service
@Slf4j
@RequiredArgsConstructor
public class RoleWithoutInstitutionManager implements RoleManager {

    private final RoleService roleService;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public void verifyAndAddPermissions(PermissionInfo info) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("verify And Add Permissions for Role Admin_Infra to user %s", info.getUserId().toString()));
        }
        roleService.checkRoleNotAlreadyAssigned(info.getUserId());
        final var attValues = new HashMap<String, List<String>>();
        String description = keycloakAdminClient.getRoleById(info.getRoleId().toString()).getDescription();
        attValues.put(DESCRIPTION_ROLE, List.of(description));
        roleService.addUserRoleInKeycloak(info.getUserId(), info.getRoleId(), attValues);
    }
}
