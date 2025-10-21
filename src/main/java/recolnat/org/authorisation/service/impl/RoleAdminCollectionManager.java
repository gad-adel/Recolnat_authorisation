package recolnat.org.authorisation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import recolnat.org.authorisation.api.domain.PermissionInfo;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;
import recolnat.org.authorisation.service.CollectionCheck;
import recolnat.org.authorisation.service.RoleManager;
import recolnat.org.authorisation.service.RoleService;

import java.util.HashMap;
import java.util.List;

import static recolnat.org.authorisation.service.impl.RoleServiceImpl.DESCRIPTION_ROLE;
import static recolnat.org.authorisation.service.impl.UserServiceImpl.COLLECTIONS_KEY;
import static recolnat.org.authorisation.service.impl.UserServiceImpl.INSTITUTION_KEY;


@Service
@Slf4j
@RequiredArgsConstructor
public class RoleAdminCollectionManager implements RoleManager {

    private final RoleService roleService;
    private final KeycloakAdminClient keycloakAdminClient;

    /**
     * call client web pour checked les collections
     */
    @Override
    public void verifyAndAddPermissions(PermissionInfo info) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("verify And Add Permissions for Role Admin_Collection to user %s", info.getUserId().toString()));
        }
        // check input is valid
        roleService.validatePermissionInfo(info, CollectionCheck.class);
        final var inst = roleService.checkInstitutionExist(info.getInstitutionId());
        roleService.checkCurrentUserMemberOfInstitution(info.getInstitutionId());
        roleService.checkAssignementRepetable(info.getUserId());

        roleService.checkAdminInstitutionCollections(info.toCollectionsAsString());

        final var attValues = new HashMap<String, List<String>>();
        attValues.put(INSTITUTION_KEY, List.of(String.valueOf(inst)));
        attValues.put(COLLECTIONS_KEY, info.toCollectionsAsString());
        String description = keycloakAdminClient.getRoleById(info.getRoleId().toString()).getDescription();
        attValues.put(DESCRIPTION_ROLE, List.of(description));
        roleService.addUserRoleInKeycloak(info.getUserId(), info.getRoleId(), attValues);
    }
}
