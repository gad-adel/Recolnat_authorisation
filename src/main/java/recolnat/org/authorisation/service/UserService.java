package recolnat.org.authorisation.service;

import io.recolnat.model.UnassignedUserDTO;
import recolnat.org.authorisation.api.domain.UserProfile;
import recolnat.org.authorisation.api.domain.UserProfilePage;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Retourne la liste des utilisateurs (par page)
     *
     * @param page          index de la page à récupérer
     * @param size          taille de la page
     * @param searchTerm    terme à rechercher dans le nom de l'utilisateur
     * @param institutionId identifiant de l'institution recherchée
     * @return une liste d'utilisateurs paginée
     */
    UserProfilePage findAll(int page, int size, String searchTerm, UUID institutionId);

    /**
     * Retourne l'utilisateur connecté
     *
     * @return un utilisateur
     */
    UserProfile getCurrentUser();

    /**
     * Recherche un utilisateur via son UUID
     *
     * @param userId identifiant de l'utilisateur recherché
     * @return un utilisateur
     */
    UserProfile getUserByUid(UUID userId);

    /**
     * Retourne l'ensemble des utilisateurs sans role.
     *
     * @return une liste d'utilisateur
     */
    List<UnassignedUserDTO> findAllUnassignedUsers();
}
