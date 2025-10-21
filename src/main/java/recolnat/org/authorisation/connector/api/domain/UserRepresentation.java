package recolnat.org.authorisation.connector.api.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.json.StringListMapDeserializer;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserConsentRepresentation;
import org.keycloak.representations.idm.UserProfileMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class UserRepresentation {

	
	 	protected String self; // link
	    protected String id;
	    protected String origin;
	    protected Long createdTimestamp;
	    /**
	     * nom commun du user
	     */
	    protected String username;
	    /**
	     * user actif
	     */
	    protected Boolean enabled;
	    protected Boolean totp;
	    /**
	     * doit être à true
	     */
	    protected Boolean emailVerified;
	    /**
	     * nom  du user
	     */
	    protected String firstName;
	    /**
	     * prenom  du user
	     */
	    protected String lastName;
	    protected String email;
	    protected String serviceAccountClientId; // For rep, it points to clientId (not DB ID)

	    @JsonDeserialize(using = StringListMapDeserializer.class)
	    protected Map<String, List<String>> attributes;
	    /**
	     * example:
	     *  CredentialRepresentation passwordCred = new CredentialRepresentation();
	     *  passwordCred.setTemporary(false);
	     *  passwordCred.setType(CredentialRepresentation.PASSWORD);
	     *  passwordCred.setValue("test");
	     */
	    protected List<CredentialRepresentation> credentials;
	    protected Set<String> disableableCredentialTypes;
	    protected List<String> requiredActions;
	    protected List<String> realmRoles;
	    protected Map<String, List<String>> clientRoles;
	    protected List<UserConsentRepresentation> clientConsents;
	    protected Integer notBefore;
	    
	    protected List<String> groups;
	    private Map<String, Boolean> access;
	    private UserProfileMetadata userProfileMetadata;
}
