package recolnat.org.authorisation.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

	private String jwtToken;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, 
			FilterChain filterChain)
			throws ServletException, IOException {
		setBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
		filterChain.doFilter(request, response);
	}

	private void setBearerToken(String header) {
		if(header != null && header.startsWith("Bearer")) {
			jwtToken =  header.split(" ")[1].trim();
		}
	}
	
	public Optional<String> getJwtToken() {
		return Optional.ofNullable(jwtToken);
	}
}
