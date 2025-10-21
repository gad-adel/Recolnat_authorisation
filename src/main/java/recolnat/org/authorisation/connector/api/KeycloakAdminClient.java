package recolnat.org.authorisation.connector.api;

import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public interface KeycloakAdminClient {
	
	/**
	 * ajout ,dans keycloak, du role sur un utilisateur sans attributs
	 * @param uid
	 * @param roleId
	 */
     void  addRole(String uid, String roleId);
     
     /**
      * ajout ,dans keycloak, du role sur un utilisateur ainsi que ses attributs (institutions,collections, description)
      * @param uid
      * @param roleId
      * @param attributes
      */
     void addRoleWithAttributes(String uid, String roleId, Map<String, List<String>> attributes);
     
     /**
      * suppression ,dans keycloak, du role sur un utilisateur sans attributs
      * @param uid
      * @param roleId
      * @return
      */
     OptionalInt removeRole(String uid, String roleId);
    
     /**
      * retourne la liste des utilisateurs keycloak
      * @return
      */
     List<UserRepresentation> getUsers();
     
     /**
      * retourne role informations asscoié a un utilisateur donné
      * @param uid
      * @return
      */
     List<RoleRepresentation> getRoleByUserId(String uid);

     /**
      * mise a jour d un utilisateur 
      * @param userRepresentation
      */
    void updateUser(UserRepresentation userRepresentation);
    
    /**
     * creation d un utilisateur 
     * @param userRepresentation
     */
    int createUser(UserRepresentation userRepresentation);

    /**
     * suppression d un utilisateur 
     * @param id
     * @return
     */
    int deleteUser(final String id);

    /**
     * get role informations by id role keycloak
     * @param uid
     * @return
     */
    RoleRepresentation getRoleById(String uid);
    

    /**
     *  get user informations from keycloak
     * @param uid
     * @return
     */
    UserRepresentation getUserById(String uid);
    
    /**
     * suppression ,dans keycloak, du role sur un utilisateur ainsi que ses attributs (institutions,collections, description)
     * @param toString
     * @param toString1
     * @param attValues
     */
    void removeRole(String toString, String toString1, Map<String, List<String>> attValues);
    
    /**
     * return list roles in keycloak
     * @return
     */
    public List<RoleRepresentation> getRoles();

}
