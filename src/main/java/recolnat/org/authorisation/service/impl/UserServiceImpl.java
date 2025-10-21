package recolnat.org.authorisation.service.impl;

import com.google.common.collect.Lists;
import io.recolnat.model.UnassignedUserDTO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import recolnat.org.authorisation.api.domain.Institution;
import recolnat.org.authorisation.api.domain.Role;
import recolnat.org.authorisation.api.domain.UserProfile;
import recolnat.org.authorisation.api.domain.UserProfilePage;
import recolnat.org.authorisation.api.domain.enums.ModeEnum;
import recolnat.org.authorisation.api.domain.enums.RoleEnum;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;
import recolnat.org.authorisation.common.exception.AuthorisationTechnicalException;
import recolnat.org.authorisation.common.mapper.InstitutionMapper;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;
import recolnat.org.authorisation.repository.jpa.InstitutionRepositoryJPA;
import recolnat.org.authorisation.service.AuthenticationService;
import recolnat.org.authorisation.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.springframework.util.CollectionUtils.isEmpty;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.getAssignableRole;
import static recolnat.org.authorisation.api.domain.enums.RoleEnum.isInstitutionRoleLevel;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * attribut keycloak de declaration d'attribut d institution
     */
    public static final String INSTITUTION_KEY = "institution";
    /**
     * attribut keycloak de declaration d'attribut de collection
     */
    public static final String COLLECTIONS_KEY = "collections";
    public static final String AUTH_TECH_1 = "AUTH_TECH_1";


    private final InstitutionRepositoryJPA institutionRepository;
    private final InstitutionMapper institutionMapper;
    private final KeycloakAdminClient keycloakAdminClient;
    private final AuthenticationService authenticationService;

    /**
     * Filtrage des utilisateurs 'visibles' en fonction de l'utilisateur connecté
     *
     * @param current
     * @param assignee
     * @return
     */
    private static boolean filterByRole(UserProfile current, UserProfile assignee) {
        if (isNull(assignee)) {
            return false;
        }
        final var userInst = nonNull(assignee.getInstitution()) ? assignee.getInstitution().getId() : null;
        final var userRoleCode = nonNull(assignee.getRole()) ? assignee.getRole().getCode() : null;
        final var currentInst = nonNull(current.getInstitution()) ? current.getInstitution().getId() : null;
        final var currentRoleCode = nonNull(current.getRole()) ? current.getRole().getCode() : null;

        var assignableRoles = getAssignableRole(currentRoleCode, ModeEnum.LIST);
        if (isInstitutionRoleLevel(currentRoleCode)) {
            return (nonNull(currentInst) && currentInst.equals(userInst) && assignableRoles.contains(userRoleCode)) || (isNull(userRoleCode));
        }

        return isNull((userRoleCode)) || (!isEmpty(assignableRoles) && assignableRoles.contains(userRoleCode));
    }

    /**
     * Traitement d'exception sur la creation d'un profil d'utilisateur
     *
     * @param e
     * @param uid
     */
    public static void handleException(Exception e, String uid) {
        if (StringUtils.contains(e.getMessage(), String.valueOf(HttpStatus.NOT_FOUND.value()))) {
            var techEx =
                    new AuthorisationTechnicalException(
                            "AUTH_FUNC_NFE",
                            e.getMessage().concat(":").concat(uid),
                            HttpStatus.NOT_FOUND.value(),
                            "cannot retrieve resource from auth server for value: " + uid);
            log.error(e.getMessage(), e, techEx);
            throw techEx;
        } else {
            var techEx =
                    new AuthorisationTechnicalException(
                            AUTH_TECH_1,
                            e.getMessage(),
                            HttpStatus.SERVICE_UNAVAILABLE.value(),
                            "cannot retrieve resource from auth server");
            log.error(e.getMessage(), e, techEx);
            throw techEx;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfile getCurrentUser() {
        final var uid = authenticationService.getConnected().getUserId();
        return getUserByUid(uid);
    }

    @Override
    public List<UnassignedUserDTO> findAllUnassignedUsers() {
        try {
            List<UserRepresentation> userRepresentations = keycloakAdminClient.getUsers();

            return userRepresentations
                    .stream()
                    .filter(user -> user.isEnabled() && user.isEmailVerified())
                    .map(this::buildUserProfile)
                    .filter(Objects::nonNull)
                    .filter(userp -> userp.getRole() == null)
                    .map((userRepresentation -> {
                        UnassignedUserDTO dto = new UnassignedUserDTO();
                        dto.setUid(userRepresentation.getUid());
                        dto.setEmail(userRepresentation.getEmail());
                        dto.setFirstName(userRepresentation.getFirstName());
                        dto.setLastName(userRepresentation.getLastName());
                        return dto;
                    }))
                    .sorted(comparing(u -> u.getLastName() + " " + u.getFirstName()))
                    .toList();

        } catch (Exception e) {
            // exception handler
            var techEx = new AuthorisationTechnicalException(AUTH_TECH_1, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "cannot retrieve all users from auth server");
            log.error(e.getMessage(), e, techEx);
            throw techEx;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfile getUserByUid(@NotNull UUID uuid) {
        UserProfile userProfile = null;

        try {
            final var user = keycloakAdminClient.getUserById(uuid.toString());
            userProfile = buildUserProfile(user);

        } catch (Exception e) {
            // exception handler
            handleException(e, uuid.toString());
        }
        return userProfile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProfilePage findAll(int page, int size, String searchTerm, UUID institutionId) {
        List<UserRepresentation> userRepresentations;
        var currentUser = getCurrentUser();

        try {
            userRepresentations = keycloakAdminClient.getUsers();
        } catch (Exception e) {
            // exception handler
            var techEx = new AuthorisationTechnicalException(AUTH_TECH_1, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "cannot retrieve all users from auth server");
            log.error(e.getMessage(), e, techEx);
            throw techEx;
        }

        if (isEmpty(userRepresentations)) {
            return UserProfilePage.builder().users(new ArrayList<>()).totalPages(0).build();
        }

        var users = userRepresentations
                .stream()
                .filter(user -> user.isEnabled() && user.isEmailVerified())
                .map(this::buildUserProfile)
                .sorted(comparing(u -> u != null ? u.getLastName() + " " + u.getFirstName() : null, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (StringUtils.isNotBlank(searchTerm)) {
            users = users.stream().filter(u -> {
                var fullname = u.getLastName() + " " + u.getFirstName();
                return fullname.toUpperCase(Locale.ROOT).contains(searchTerm.toUpperCase(Locale.ROOT));
            }).toList();
        }

        if (institutionId != null) {
            users = users.stream().filter(u -> filterByRole(currentUser, u)).toList();
            users = users.stream().filter(u -> u.getInstitution() != null && u.getInstitution().getInstitutionId().equals(institutionId)).toList();
        }

        var allPages = Lists.partition(users, size);
        if (page >= allPages.size()) {
            log.warn("ERR_NFE_CODE", "Page: " + page + " cannot be requested because no user with roles cannot be found ");
            return UserProfilePage.builder().users(new ArrayList<>()).totalPages(0).build();
        }
        var userOfPage = allPages.get(page);

        return UserProfilePage.builder().users(userOfPage).totalPages(allPages.size()).numberOfElements(users.size()).build();
    }

    /**
     * Construit un objet profile d'un utilisateur.
     * Si une exception apparait concernant l'utilisateur (institution inexistante....)
     * le UserProfile sera null et ignoré (seul, un message d'erreur apparaitra dans les logs)
     *
     * @param userRepresentation
     * @return
     */
    private UserProfile buildUserProfile(UserRepresentation userRepresentation) {
        if (nonNull(userRepresentation)) {

            try {
                return UserProfile.builder()
                        .uid(UUID.fromString(userRepresentation.getId()))
                        .username(userRepresentation.getUsername())
                        .email(userRepresentation.getEmail())
                        .firstName(userRepresentation.getFirstName())
                        .lastName(userRepresentation.getLastName())
                        .role(buildRole(userRepresentation.getId()))
                        .institution(buildInst(userRepresentation.getAttributes(), userRepresentation.getUsername()))
                        .collections(buildCollections(userRepresentation.getAttributes()))
                        .build();
            } catch (AuthorisationBusinessException e) {
                log.error(e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Construit l'objet institution de l'utilisateur
     * si une institution est trouvé pour un utilisateur (une verification est faite de l'existence de l'institution)
     *
     * @param attributes
     * @param user
     * @return
     */
    private Institution buildInst(Map<String, List<String>> attributes, String user) {
        if (nonNull(attributes) && !isEmpty(attributes.get(INSTITUTION_KEY))) {

            final var allInst = attributes.get(INSTITUTION_KEY);

            if (!isEmpty(allInst)) {
                final var instId = allInst.stream().filter(Objects::nonNull).findFirst();
                if (instId.isPresent()) {
                    final var instJPA = institutionRepository.findById(Integer.valueOf(instId.get())).orElseThrow(() ->
                            new AuthorisationBusinessException(HttpStatus.NOT_FOUND, "AUTH_FUNC_NFE", "Institution cannot be found with id: " + instId + ", for user: " + user));
                    return institutionMapper.toInstitution(instJPA);
                }
            }
        }
        return null;
    }

    /**
     * Construit la liste des collections associées à l'utilisateur
     *
     * @param attributes
     * @return
     */
    private Set<String> buildCollections(Map<String, List<String>> attributes) {
        if (nonNull(attributes)) {
            List<String> cols = attributes.get(COLLECTIONS_KEY);
            if (!isEmpty(cols)) {
                return cols.stream().collect(toUnmodifiableSet());
            }
        }
        return Set.of();
    }

    /**
     * Un user has only one role
     * construction de l'objet role de l'utilisateur
     *
     * @param uid
     * @return
     */
    private Role buildRole(String uid) {
        try {
            final var roleByUserId = keycloakAdminClient.getRoleByUserId(uid);
            if (!isEmpty(roleByUserId)) {
                final var role = roleByUserId.stream()
                        .filter(roleRepresentation -> RoleEnum.findRoleEnumByName(roleRepresentation.getName()).isPresent())
                        .findFirst().orElse(null);

                return nonNull(role)
                        ? Role.builder()
                        .id(role.getId())
                        .code(RoleEnum.fromValue(role.getName()))
                        .name(role.getName())
                        .description(role.getDescription())
                        .build()
                        : null;
            }

        } catch (Exception e) {
            handleException(e, uid);
        }
        return null;
    }
}
