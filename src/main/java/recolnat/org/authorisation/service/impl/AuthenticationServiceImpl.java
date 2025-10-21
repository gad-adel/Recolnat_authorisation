package recolnat.org.authorisation.service.impl;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import recolnat.org.authorisation.api.domain.ConnectedUser;
import recolnat.org.authorisation.service.AuthenticationService;

import java.util.UUID;
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
	
    public static final String SUB = "sub";
    
    @Override
    public ConnectedUser getConnected() {
        final var  auth = (OAuth2IntrospectionAuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final var userName = (String) auth.getAttribute("username");
        return ConnectedUser.builder()
            .userId(UUID.fromString(auth.getClaimAsString(SUB)))
            .userName(userName).build();
    }
}
