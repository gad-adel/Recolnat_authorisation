package recolnat.org.authorisation.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recolnat.org.authorisation.api.domain.PermissionInfo;
import recolnat.org.authorisation.api.domain.Role;
import recolnat.org.authorisation.api.domain.UserProfile;
import recolnat.org.authorisation.api.domain.enums.ModeEnum;
import recolnat.org.authorisation.api.domain.enums.RoleEnum;
import recolnat.org.authorisation.common.config.AuthProperties;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;
import recolnat.org.authorisation.connector.api.CollectionApiClient;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;
import recolnat.org.authorisation.connector.api.domain.OutputCollection;
import recolnat.org.authorisation.repository.jpa.InstitutionRepositoryJPA;
import recolnat.org.authorisation.service.RoleManager;
import recolnat.org.authorisation.service.RoleService;
import recolnat.org.authorisation.service.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.ADMIN;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.getAssignableRole;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.isCollectionMaster;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.isDataEntry;
import static recolnat.org.authorisation.service.impl.UserServiceImpl.handleException;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    /**
     * Attribut keycloak de description de role
     */
    public static final String DESCRIPTION_ROLE = "description";

    public static final String ACCESS_INFORM_COD = "ACCESS_INFORM_COD";
    public static final String ACCESS_DENIED_COD = "ACCESS_DENIED_COD";
    public static final String UNAUTHORISED_ACTION_WITH_YOUR_ROLE = " Unauthorised action with your role :";
    public static final String ALREADY_ASSIGNED_FOR = "already assigned for";
    public static final String ROLE = " Role: ";
    public static final String AUTH_FUNC_NFE = "AUTH_FUNC_NFE";

    private final InstitutionRepositoryJPA institutionRepository;
    private final CollectionApiClient collectionService;
    private final Keycloak keycloak;
    private final KeycloakAdminClient keycloakAdminClient;
    private final UserService userService;
    private final AuthProperties authProperties;
    private final Validator validator;
    /**
     * Retourne l'implémentation qui va être employé pour verifier et ajouter les roles
     */
    BiFunction<RoleEnum, KeycloakAdminClient, RoleManager> roleManagerFunction = (roleEnum, kcAdminClient) ->
            switch (roleEnum) {
                case USER_INFRA, ADMIN -> new RoleWithoutInstitutionManager(this, kcAdminClient);

                case ADMIN_INSTITUTION -> new RoleAdminInstitutionManager(this, kcAdminClient);

                case ADMIN_COLLECTION, DATA_ENTRY -> new RoleAdminCollectionManager(this, kcAdminClient);

                default -> {
                    final var businessException = new AuthorisationBusinessException(ACCESS_DENIED_COD, UNAUTHORISED_ACTION_WITH_YOUR_ROLE + roleEnum.name());
                    log.error(businessException.getMessage(), businessException);
                    throw businessException;
                }
            };

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> retrieveAllRole() {
        List<RoleEnum> activeRoles = new ArrayList<>();
        final var currentUser = userService.getCurrentUser();
        var currentRole = currentUser.getRole();
        if (nonNull(currentRole)) {
            activeRoles.addAll(getAssignableRole(currentRole.getCode(), ModeEnum.LIST));
        }

        var finalActiveRoles = new ArrayList<>(activeRoles);
        var roles = keycloak.realm(authProperties.getAppRealm()).roles().list();

        return roles.stream()
                .filter(role -> RoleEnum.findRoleEnumByName(role.getName()).isPresent())
                .map(roleRepresentation ->
                {
                    String name = roleRepresentation.getName();
                    return Role.builder()
                            .id(roleRepresentation.getId())
                            .name(name)
                            .code(StringUtils.isNoneBlank(name) ? RoleEnum.fromValue(name) : null)
                            .description(roleRepresentation.getDescription()).build();
                }).map(role -> {
                    if (!org.springframework.util.CollectionUtils.isEmpty(finalActiveRoles) && finalActiveRoles.contains(role.getCode())) {
                        return Role.builder()
                                .id(role.getId())
                                .name(role.getName())
                                .code(role.getCode())
                                .assignable(true)
                                .description(role.getDescription()).build();
                    }
                    return role;
                }).sorted(Comparator.comparing(Role::isAssignable).reversed()).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkRoleNotAlreadyAssigned(UserProfile userGrantee) {
        Optional.ofNullable(userGrantee.getRole()).map(Role::getCode)
                .ifPresent(role -> {
                    final var authExc = new AuthorisationBusinessException(ACCESS_INFORM_COD, ROLE + role + " " + ALREADY_ASSIGNED_FOR + " :" + userGrantee.getUsername());
                    log.error(authExc.getMessage(), authExc);
                    throw authExc;
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkRoleNotAlreadyAssigned(UUID userId) {
        var user = userService.getUserByUid(userId);
        Optional.ofNullable(user.getRole()).map(Role::getCode)
                .filter(Objects::nonNull)
                .ifPresent(role -> {
                    final var authExc = new AuthorisationBusinessException(ACCESS_INFORM_COD, ROLE + role + " " + ALREADY_ASSIGNED_FOR + " :" + user.getUsername());
                    log.error(authExc.getMessage(), authExc);
                    throw authExc;
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeUserPermission(UUID uid) {
        final var user = userService.getUserByUid(uid);
        final var currentUser = userService.getCurrentUser();
        final var currentRole = Optional.ofNullable(currentUser)
                .orElseThrow(() -> new AuthorisationBusinessException(ACCESS_DENIED_COD, " Resource not found with id :" + currentUser)).getRole();
        final var userRole = Optional.ofNullable(user)
                .orElseThrow(() -> new AuthorisationBusinessException(ACCESS_DENIED_COD, " User Resource not found with id :" + uid)).getRole();
        checkRoleIsAssignable(currentRole, userRole, ModeEnum.REMOVE);
        if (isDataEntry(userRole.getCode()) || isCollectionMaster(userRole.getCode())) {

            final var userInst = Optional.ofNullable(user.getInstitution()).orElseThrow(
                    () -> new AuthorisationBusinessException(AUTH_FUNC_NFE, "No institutionId assigned")).getInstitutionId();
            checkCurrentUserMemberOfInstitution(userInst);
        }

        keycloakAdminClient.removeRole(user.getUid().toString(), userRole.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addUserPermission(UUID uid, PermissionInfo info) {

        final var perm = PermissionInfo.builder()
                .userId(uid)
                .roleId(info.getRoleId())
                .institutionId(info.getInstitutionId())
                .collections(info.getCollections()).build();

        validatePermissionInfo(perm, Default.class);

        userService.getUserByUid(perm.getUserId());//check user

        final var roleToAdd = checkRoleExist(perm.getRoleId().toString());

        checkRoleIsAssignable(userService.getCurrentUser().getRole(), roleToAdd, ModeEnum.ADD);

        Optional.ofNullable(roleToAdd).filter(Objects::nonNull).map(Role::getCode)
                .filter(Objects::nonNull)
                .map(r -> roleManagerFunction.apply(r, keycloakAdminClient))
                .orElseThrow(
                        () -> {
                            final var businessException = new AuthorisationBusinessException(ACCESS_DENIED_COD, UNAUTHORISED_ACTION_WITH_YOUR_ROLE + roleToAdd);
                            log.error(businessException.getMessage(), businessException);
                            throw businessException;
                        }).verifyAndAddPermissions(perm);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer checkInstitutionExist(UUID institutionId) {
        return institutionRepository
                .findByInstitutionId(institutionId)
                .orElseThrow(() ->
                        new AuthorisationBusinessException(AUTH_FUNC_NFE,
                                "institutionId not found with id :" + institutionId))
                .getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkCurrentUserMemberOfInstitution(UUID institutionId) {
        var currentUser = userService.getCurrentUser();

        // Les admins ne sont pas liés à une institution, on ne fait donc pas de contrôle
        if (currentUser.getRole().getCode().equals(ADMIN)) {
            return;
        }

        var curInst = Optional.ofNullable(currentUser.getInstitution()).orElseThrow(() ->
                        new AuthorisationBusinessException(AUTH_FUNC_NFE,
                                "No institutionId assigned for user : " + currentUser.getUsername()))
                .getInstitutionId();

        if (nonNull(institutionId) && !institutionId.equals(curInst)) {
            var ex = new AuthorisationBusinessException(
                    AUTH_FUNC_NFE, String.format("You are not member of the institution with id: %s", institutionId));
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAssignementRepetable(UUID userId) {
        var user = userService.getUserByUid(userId);
        var role = user.getRole();
        if (nonNull(role) && !RoleEnum.ADMIN_COLLECTION.equals(role.getCode())) {
            final var ex = new AuthorisationBusinessException(ACCESS_INFORM_COD, ROLE + role.getCode() + " " + ALREADY_ASSIGNED_FOR + " :" + user.getUsername());
            log.error(ex.getMessage(), ex);
            throw ex;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addUserRoleInKeycloak(UUID userId, UUID roleId, Map<String, List<String>> atts) {
        keycloakAdminClient.addRoleWithAttributes(userId.toString(), roleId.toString(), atts);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addUserRoleInKeycloak(UUID userId, UUID roleId) {
        keycloakAdminClient.addRole(userId.toString(), roleId.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void checkAdminInstitutionCollections(List<String> collectionIdsToAdd) {
        var currentUser = userService.getCurrentUser();

        // Les admins ne sont pas liés à une institution, on ne fait donc pas de contrôle
        if (currentUser.getRole().getCode().equals(ADMIN)) {
            return;
        }

        List<OutputCollection> collectionsByInstitutions;
        try {
            collectionsByInstitutions = collectionService.retrieveCollectionsByInstitutions(currentUser.getInstitution().getId());
        } catch (Exception e) {
            var ex = new AuthorisationBusinessException(ACCESS_DENIED_COD, e.getMessage());
            log.error(ex.getMessage(), ex);
            throw ex;
        }

        if (isEmpty(collectionsByInstitutions)) {
            var ex = new AuthorisationBusinessException(ACCESS_DENIED_COD, " institution has no collections: " + collectionIdsToAdd);
            log.error(ex.getMessage(), ex);
            throw ex;
        }

        final var allIds = collectionsByInstitutions.stream().map(OutputCollection::getId).map(UUID::toString).toList();

        if (collectionIdsToAdd.stream().anyMatch(id -> !allIds.contains(id))) {
            var ex = new AuthorisationBusinessException(ACCESS_DENIED_COD, " collections: " + collectionIdsToAdd + " not found in list : " + allIds);
            log.error(ex.getMessage(), ex);
            throw ex;
        }

    }

    /**
     * {@inheritDoc}
     */
    public void validatePermissionInfo(PermissionInfo info, Class<?>... checks) {
        final var violations = validator.validate(info, checks);
        if (!CollectionUtils.isEmpty(violations)) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
            throw new AuthorisationBusinessException(ACCESS_INFORM_COD, message);
        }
    }

    /**
     * Vérifie si le role UUID, dans keycloak (database) est present
     * et construit un objet role
     *
     * @param roleId identifiant du role
     * @return
     */
    private Role checkRoleExist(String roleId) {
        RoleRepresentation role = null;
        try {
            role = keycloakAdminClient.getRoleById(roleId);
        } catch (Exception e) {
            handleException(e, roleId);
        }

        final var role1 = Optional.ofNullable(role).filter(roleRepresentation -> nonNull(roleRepresentation)
                                                                                 && RoleEnum.findRoleEnumByName(roleRepresentation.getName()).isPresent())
                .orElseThrow(() -> new AuthorisationBusinessException(HttpStatus.BAD_REQUEST, ACCESS_INFORM_COD, " Role does not exist  :" + roleId));

        return Role.builder()
                .id(role1.getId())
                .code(RoleEnum.fromValue(role1.getName()))
                .name(role1.getName())
                .description(role1.getDescription())
                .build();
    }

    /**
     * Indique si un rôle est assignable (possibilité de l'affecter, de le retirer d'un utilisateur ou de le lister) pour un rôle donné dans un mode donnée
     *
     * @param source rôle demandant l'action
     * @param target rôle cible
     * @param mode   mode demandé (Ajout ou retrait du role ou listing)
     */
    private void checkRoleIsAssignable(final Role source, final Role target, ModeEnum mode) {
        final var currentRole = Optional.ofNullable(source)
                .orElseThrow(() -> new AuthorisationBusinessException(ACCESS_DENIED_COD, "You don't have no role assigned")).getCode();
        final var targetRole = Optional.ofNullable(target)
                .orElseThrow(() -> new AuthorisationBusinessException(ACCESS_DENIED_COD, "User has no role to assign or remove")).getCode();

        final var sourceCode = Optional.ofNullable(currentRole)
                .orElseThrow(() -> {
                    final var businessException = new AuthorisationBusinessException(ACCESS_DENIED_COD, UNAUTHORISED_ACTION_WITH_YOUR_ROLE + currentRole);
                    log.error(businessException.getMessage(), businessException);
                    throw businessException;
                });

        final var targetCode = Optional.ofNullable(target)
                .orElseThrow(() -> {
                    final var businessException = new AuthorisationBusinessException(ACCESS_DENIED_COD, " Unauthorised action with target role  :" + targetRole);
                    log.error(businessException.getMessage(), businessException);
                    throw businessException;
                }).getCode();

        if (!RoleEnum.isAssignableRole(sourceCode, targetCode, mode)) {
            final var businessException = new AuthorisationBusinessException(ACCESS_DENIED_COD, " Imcompatible of pair roles  :" + String.join(",", sourceCode.name(), targetRole.name()));
            log.error(businessException.getMessage(), businessException);
            throw businessException;
        }
    }

}
