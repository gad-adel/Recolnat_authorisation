package recolnat.org.authorisation.service;

import java.util.Optional;

public interface TokenService {

	Optional<String> getJwtToken();
}
