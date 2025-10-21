package recolnat.org.authorisation.connector.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;
import recolnat.org.authorisation.common.config.AuthProperties;
import recolnat.org.authorisation.common.exception.AuthorisationTechnicalException;
import recolnat.org.authorisation.connector.api.KeycloakAdminClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import static java.util.Objects.nonNull;
import static recolnat.org.authorisation.service.impl.UserServiceImpl.COLLECTIONS_KEY;
import static recolnat.org.authorisation.service.impl.UserServiceImpl.INSTITUTION_KEY;
import static recolnat.org.authorisation.service.impl.RoleServiceImpl.DESCRIPTION_ROLE;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminClientImpl implements KeycloakAdminClient {

    private final Keycloak keycloak;
    private final AuthProperties authProperties;
    
    /**
     * {@inheritDoc}
     */
   @Override
   public List<UserRepresentation>  getUsers(){
          return getRealmResource().users().list();
    }

   /**
    * {@inheritDoc}
    */
   @Override
    public List<RoleRepresentation>  getRoles(){

        try{
        final var realmResource = getRealmResource();
          return realmResource.roles().list();
        }
       catch (Exception e) {
            var techEx = new AuthorisationTechnicalException("AUTH_TECH_1",e.getMessage(),
                          HttpStatus.SERVICE_UNAVAILABLE.value(),
                          "cannot retrieve all roles from auth server");
            log.error(e.getMessage(), e, techEx);
            throw techEx;
        }
    }

   /**
    * {@inheritDoc}
    */
    @Override
    public void  addRole(String uid, String roleId){

        try{
            final var realm = getRealmResource();
            var user = realm.users().get(uid).toRepresentation();

            var role = realm.rolesById().getRole(roleId);

            log.info("user :{}, role : {}",user.getId(),role.getName());

            final var userResource = realm.users().get(user.getId());
            userResource.roles().realmLevel().add(List.of(role));
            userResource.update(user);

        }
       catch (Exception e) {
            var techEx = new AuthorisationTechnicalException("AUTH_TECH_1", e.getMessage(),
                            HttpStatus.SERVICE_UNAVAILABLE.value(),
                            "cannot retrieve all roles from auth server");
            log.error(e.getMessage(), e, techEx);
            throw techEx;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRoleWithAttributes(String uid, String roleId, Map<String, List<String>> attValues) {
        final var realm = getRealmResource();
        var user = realm.users().get(uid).toRepresentation();

        var role = realm.rolesById().getRole(roleId);
        if (nonNull(user.getAttributes())){
            user.getAttributes().putAll(attValues);
        }else {
        	user.setAttributes(attValues);
        }

        final var userResource = realm.users().get(user.getId());
        var existRoles = userResource.roles().realmLevel().listAll();
        //reset exist roles
        userResource.roles().realmLevel().remove(existRoles);
        userResource.roles().realmLevel().add(List.of(role));
        userResource.update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalInt removeRole(String uid, String roleId) {

            final var realm = getRealmResource();
            final var user = getUserById(uid);

            final var role = getRoleById(roleId);
            log.debug("user :{}, role : {}",user.getId(),role.getName());
            final var userResource = realm.users().get(user.getId());

            userResource.roles().realmLevel().remove(List.of(role));
            
	        Map<String, List<String>> attValues = new HashMap<>();
	        attValues.put(INSTITUTION_KEY, List.of());
	        attValues.put(COLLECTIONS_KEY, List.of());
	        attValues.put(DESCRIPTION_ROLE, List.of());
	        
	        if (nonNull(user.getAttributes())){
	            user.getAttributes().putAll(attValues);
	        }else {
	            user.setAttributes(attValues);
	        }
            userResource.update(user);
            userResource.logout();

        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public UserRepresentation getUserById(String uid){
            final var realm = getRealmResource();
           return realm.users().get(uid).toRepresentation();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRole(String userId, String roleId, Map<String, List<String>> attValues) {
        final var realm = getRealmResource();
        final var user = getUserById(userId);

        final var role = getRoleById(roleId);

        if (nonNull(user.getAttributes())){
            user.getAttributes().putAll(attValues);
        }else {
            user.setAttributes(attValues);
        }

        var userResource = realm.users().get(user.getId());
        userResource.roles().realmLevel().remove(List.of(role));
        userResource.update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(final UserRepresentation userRepresentation) {
        final var realm = getRealmResource();
        final var user = getUserById(userRepresentation.getId());
        final var  userResource = realm.users().get(user.getId());
        userResource.update(userRepresentation);
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public int createUser(final UserRepresentation userRepresentation) {
		 final var realm = getRealmResource();
		 try(var create = realm.users().create(userRepresentation)) {
			 return create.getStatus();
		 }
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public int deleteUser(final String id) {
		 final var realm = getRealmResource();
		 return  realm.users().delete(id).getStatus();
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleRepresentation getRoleById(String roleId){
            final var realm = getRealmResource();
           return realm.rolesById().getRole(roleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RoleRepresentation> getRoleByUserId(String uid){
        final var realm = getRealmResource();
        final var user = getUserById(uid);
        final var userResource = realm.users().get(user.getId());
        return userResource.roles().realmLevel().listEffective();
    }

    /**
     * keycloak instance
     * @return
     */
    private  RealmResource getRealmResource() {
        return keycloak.realm(authProperties.getAppRealm());
    }
    
}


