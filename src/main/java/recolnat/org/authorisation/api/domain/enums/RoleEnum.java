package recolnat.org.authorisation.api.domain.enums;


import jakarta.validation.constraints.NotNull;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public enum RoleEnum {
    ADMIN,
    USER_INFRA,
    ADMIN_INSTITUTION,
    ADMIN_COLLECTION,
    DATA_ENTRY,
    USER;

    public static final String ROLE_NFE_CODE = "ROLE_NFE_CODE";

    /**
     * Indique si un rôle source a le droit d'agir sur un rôle cible et par extension les utilisateurs ayant ce rôle.
     * <p>
     * Ex: le user connected doit être ADMIN pour modifier des droits sur un utilisateur ayant le role ADMIN_INSTITUTION ou USER_INFRA<br>
     *
     * @param source rôle source
     * @param target rôle cible
     * @return si le rôle source a le droit sur le rôle cible
     */
    public static boolean isAssignableRole(@NotNull RoleEnum source, @NotNull RoleEnum target, ModeEnum mode) {
        return switch (source) {
            case ADMIN_INSTITUTION, ADMIN -> getAssignableRole(source, mode).contains(target);
            default -> throw new AuthorisationBusinessException(ROLE_NFE_CODE, "Owner with role: " + source + " cannot grant permission:" + target);
        };
    }

    /**
     * has role ADMIN_INSTITUTION
     *
     * @param role
     * @return
     */
    public static boolean isInstitution(RoleEnum role) {
        return ADMIN_INSTITUTION.equals(role);
    }

    /**
     * has role DATA_ENTRY
     *
     * @param role
     * @return
     */
    public static boolean isDataEntry(RoleEnum role) {
        return DATA_ENTRY.equals(role);
    }

    /**
     * has role ADMIN_COLLECTION
     *
     * @param role
     * @return
     */
    public static boolean isCollectionMaster(RoleEnum role) {
        return ADMIN_COLLECTION.equals(role);
    }

    /**
     * Check if the ROLE is defined
     *
     * @param value
     * @return
     */
    public static RoleEnum fromValue(String value) {
        for (RoleEnum b : values()) {
            if (b.name().equals(value)) {
                return b;
            }
        }
        throw new AuthorisationBusinessException(ROLE_NFE_CODE, "Unexpected value '" + value + "'");
    }

    /**
     * Liste de rôles pouvant être gérés par un rôle donné pour un mode donné.
     *
     * @param currentRole rôle demandé
     * @param mode        mode demandé
     * @return une liste de rôles
     */
    public static List<RoleEnum> getAssignableRole(RoleEnum currentRole, ModeEnum mode) {
        List<RoleEnum> result = new ArrayList<>();
        if (isNull(currentRole)) {
            return result;
        }
        switch (currentRole) {
            case ADMIN -> {
                if (Objects.requireNonNull(mode) == ModeEnum.REMOVE) {
                    result.addAll(List.of(USER_INFRA, ADMIN_INSTITUTION, ADMIN_COLLECTION, DATA_ENTRY));
                } else {
                    result.addAll(List.of(ADMIN, USER_INFRA, ADMIN_INSTITUTION, ADMIN_COLLECTION, DATA_ENTRY));
                }
            }
            case ADMIN_INSTITUTION -> result.addAll(List.of(DATA_ENTRY, ADMIN_COLLECTION));
            default -> result.addAll(new ArrayList<>());
        }

        return result;
    }

    public static boolean isInstitutionRoleLevel(RoleEnum role) {
        return nonNull(role) && (DATA_ENTRY.equals(role) || ADMIN_INSTITUTION.equals(role) || ADMIN_COLLECTION.equals(role));
    }

    /**
     * get optional role from name Role
     *
     * @param name
     * @return
     */
    public static Optional<RoleEnum> findRoleEnumByName(String name) {
        return Arrays.stream(values()).filter(role -> role.name().equals(name)).findFirst();
    }

    /**
     * find role by IgnoreCase
     *
     * @param value
     * @return
     */
    public static boolean isFunctionallRole(String value) {
        return EnumSet.allOf(RoleEnum.class).stream().anyMatch(role -> role.name().equalsIgnoreCase(value));
    }
}

