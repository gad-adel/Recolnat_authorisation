package recolnat.org.authorisation.web;


import io.recolnat.api.HandleUserPermissionsApi;
import io.recolnat.api.RetrieveAllRolesApi;
import io.recolnat.model.PermissionRequestDTO;
import io.recolnat.model.RoleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;
import recolnat.org.authorisation.common.exception.AuthorisationTechnicalException;
import recolnat.org.authorisation.common.mapper.PermissionMapper;
import recolnat.org.authorisation.common.mapper.RoleMapper;
import recolnat.org.authorisation.service.RoleService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;


@RestController
@RequiredArgsConstructor
@Slf4j
public class RoleResource implements RetrieveAllRolesApi, HandleUserPermissionsApi {

    private static final String APPLICATIVE_ERROR = "Applicative Error";
    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return RetrieveAllRolesApi.super.getRequest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<RoleResponseDTO>> getRoles() {

        log.info(" Display all roles");
        try {
            final var roles = roleService.retrieveAllRole().stream()
                    .map(roleMapper::roleToRoleDto).toList();
            return new ResponseEntity<>((roles), OK);
        } catch (AuthorisationBusinessException | AuthorisationTechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthorisationBusinessException(HttpStatus.CONFLICT, APPLICATIVE_ERROR, e.getMessage());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> removePermission(UUID uid) {
        try {
            roleService.removeUserPermission(uid);
            return new ResponseEntity<>(buildHeader(uid), OK);
        } catch (AuthorisationBusinessException | AuthorisationTechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthorisationBusinessException(HttpStatus.CONFLICT, APPLICATIVE_ERROR, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> addPermission(UUID uid, PermissionRequestDTO permissionRequestDTO) {
        try {
            final var perm = permissionMapper.toPermissionInfo(permissionRequestDTO);
            roleService.addUserPermission(uid, perm);
            return new ResponseEntity<>(buildHeader(uid), OK);
        } catch (AuthorisationBusinessException | AuthorisationTechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthorisationBusinessException(HttpStatus.CONFLICT, APPLICATIVE_ERROR, e.getMessage());
        }
    }

    /**
     * add uid in header response
     *
     * @param userId
     * @return
     */
    private HttpHeaders buildHeader(UUID userId) {
        final var headers = new HttpHeaders();
        headers.add("uid", userId.toString());
        return headers;
    }

}
