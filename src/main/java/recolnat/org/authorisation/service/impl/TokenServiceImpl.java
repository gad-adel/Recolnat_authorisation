package recolnat.org.authorisation.service.impl;

import lombok.RequiredArgsConstructor;
import recolnat.org.authorisation.common.config.JwtTokenFilter;
import recolnat.org.authorisation.service.TokenService;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service 
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

	private final JwtTokenFilter jwtTokenFilter;
	
	@Override
	public Optional<String> getJwtToken() {
		return jwtTokenFilter.getJwtToken();
	}

}
