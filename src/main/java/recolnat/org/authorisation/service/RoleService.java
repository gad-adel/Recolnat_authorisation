package recolnat.org.authorisation.service;

import recolnat.org.authorisation.api.domain.PermissionInfo;
import recolnat.org.authorisation.api.domain.Role;
import recolnat.org.authorisation.api.domain.UserProfile;
import recolnat.org.authorisation.api.domain.enums.ModeEnum;
import recolnat.org.authorisation.api.domain.enums.RoleEnum;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RoleService {

    /**
     * Retourne la liste des roles definis dans keycloak
     *
     * @return une liste de role
     */
    List<Role> retrieveAllRole();

    /**
     * Supprime les permissions d'un utilisateur
     * voir aussi droits de suppression: {@link  RoleEnum#isAssignableRole(RoleEnum, RoleEnum, ModeEnum)}
     *
     * @param uid identifiant de l'utilisateur
     */
    void removeUserPermission(UUID uid);

    /**
     * Défini de nouvelles permissions pour un utilisateur
     *
     * @param uid  identifiant de l'utilisateur
     * @param info permissions à rajouter
     */
    void addUserPermission(UUID uid, PermissionInfo info);

    /**
     * Test la présence d'une institution en base par son id
     *
     * @param institutionId identifiant de l'institution à chercher
     * @return l'identifiant de l'institution
     */
    Integer checkInstitutionExist(UUID institutionId);

    /**
     * Validation de l'instance PermissionInfo, à partir des annotations de validation définies dans la classe
     *
     * @param info
     * @param defaultClass
     */
    void validatePermissionInfo(PermissionInfo info, Class<?>... defaultClass);

    /**
     * ajout d un role a un utilisateur dans keycloak
     *
     * @param userId
     * @param roleId
     * @param atts
     */
    void addUserRoleInKeycloak(UUID userId, UUID roleId, Map<String, List<String>> atts);

    /**
     * ajout d un role a un utilisateur dans keycloak
     *
     * @param userId
     * @param roleId
     */
    void addUserRoleInKeycloak(UUID userId, UUID roleId);

    /**
     * verifie si role non déja present dans le profile de l utilisateur<br>
     * dans le cadre de la creation de permission sur un utilisateur, celui-ci ne doit pas déjà disposer d'un role
     *
     * @param profile
     */
    void checkRoleNotAlreadyAssigned(UserProfile profile);

    /**
     * verifie si l'UUID du role non déja present à partir de l'UUID de l'utilisateur
     * <br>
     * dans le cadre de la creation de permission sur un utilisateur, celui-ci ne doit pas déjà disposer d'un role
     *
     * @param userId
     */
    void checkRoleNotAlreadyAssigned(UUID userId);

    /**
     * Recherche en base (anciennement par appel client rest) si les collections sont bien associés à l'institution.
     * l'institution est celle du userconnected (car il ne peut gérer les droits que d'utilisateurs associés à son institution
     *
     * @param colIds
     */
    void checkAdminInstitutionCollections(List<String> colIds);

    void checkCurrentUserMemberOfInstitution(UUID institutionId);

    void checkAssignementRepetable(UUID userId);
}
