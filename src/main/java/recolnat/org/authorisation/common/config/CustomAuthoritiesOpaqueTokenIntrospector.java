package recolnat.org.authorisation.common.config;

import lombok.RequiredArgsConstructor;
import recolnat.org.authorisation.api.domain.enums.RoleEnum;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@RequiredArgsConstructor
public class CustomAuthoritiesOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    public static final String REALM_ACCESS = "realm_access";
    public static final String ROLES = "roles";
    public static final String ROLE_PREFIX = "ROLE_";

    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;


    /**
     *  Introspect and verify the given token, returning its attributes.
	 *
	 * Returning a {@link Map} is indicative that the token is valid.
     */
    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
     OpaqueTokenIntrospector delegate =
            new NimbusOpaqueTokenIntrospector(oAuth2ResourceServerProperties.getOpaquetoken().getIntrospectionUri(),
                    oAuth2ResourceServerProperties.getOpaquetoken().getClientId(),
                    oAuth2ResourceServerProperties.getOpaquetoken().getClientSecret());

        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);
        return new OAuth2IntrospectionAuthenticatedPrincipal(
                principal.getName(), principal.getAttributes(), extractAuthorities(principal));    }

    
    /**
     * extract roles from principal, check by keycloak , and prefix them with ROLE_
     * By default, Spring Security adds the ROLE_ prefix for mapping rules @see RequestMatcherDelegatingAuthorizationManager<br>
     * these roles are taken from the token without a prefix on the roles, so we add the ROLE_ prefix for the Spring principal <br>
     * remark: 
     * on {@see AuthenticationServiceImpl}, to compare with Enum, these prefixes are removed
     * @param principal
     * @return
     */
    @SuppressWarnings("unchecked")
	private Collection<GrantedAuthority> extractAuthorities(OAuth2AuthenticatedPrincipal principal) {


        final Map<String, Object> realmAccess = principal.getAttribute(REALM_ACCESS);

        if (Objects.nonNull(realmAccess)){
        return ((List<String>)realmAccess.get(ROLES)).stream().filter(RoleEnum::isFunctionallRole)
                .map(roleName -> ROLE_PREFIX + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        }
        return List.of();


    }
    }

