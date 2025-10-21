package recolnat.org.authorisation.service.impl;

import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import recolnat.org.authorisation.api.domain.PermissionInfo;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;
import recolnat.org.authorisation.service.InstitutionCheck;
import recolnat.org.authorisation.service.RoleManager;
import recolnat.org.authorisation.service.RoleService;

import java.util.HashMap;
import java.util.List;

import static recolnat.org.authorisation.service.impl.RoleServiceImpl.DESCRIPTION_ROLE;
import static recolnat.org.authorisation.service.impl.UserServiceImpl.INSTITUTION_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleAdminInstitutionManager implements RoleManager {

    private final RoleService roleService;
    private final KeycloakAdminClient keycloakAdminClient;

    /**
     * remove @PreAuthorize("principal?.attributes['institution']!=null && principal?.attributes['institution']== info.institutionId")
     */
    @Override
    public void verifyAndAddPermissions(PermissionInfo info) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("verify And Add Permissions for Role Admin_Institution to user %s", info.getUserId().toString()));
        }
        roleService.checkRoleNotAlreadyAssigned(info.getUserId());
        // check input is valid
        roleService.validatePermissionInfo(info, Default.class, InstitutionCheck.class);
        final var inst = roleService.checkInstitutionExist(info.getInstitutionId());
        final var attValues = new HashMap<String, List<String>>();
        // TODO à changer pour utiliser l'uuid au lieu de l'id
        attValues.put(INSTITUTION_KEY, List.of(String.valueOf(inst)));
        String description = keycloakAdminClient.getRoleById(info.getRoleId().toString()).getDescription();
        attValues.put(DESCRIPTION_ROLE, List.of(description));
        roleService.addUserRoleInKeycloak(info.getUserId(), info.getRoleId(), attValues);
    }

}
