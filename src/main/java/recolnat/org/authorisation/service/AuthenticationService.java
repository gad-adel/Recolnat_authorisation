package recolnat.org.authorisation.service;

import recolnat.org.authorisation.api.domain.ConnectedUser;

public interface AuthenticationService {
    ConnectedUser getConnected();
}
