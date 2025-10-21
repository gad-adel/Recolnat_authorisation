package recolnat.org.authorisation.service;


import recolnat.org.authorisation.api.domain.PermissionInfo;

public interface RoleManager {
	/**
	 * differentes implementations selon le Role assigné a l'utilisateur
	 * @param info
	 */
   void  verifyAndAddPermissions(PermissionInfo info);
}
