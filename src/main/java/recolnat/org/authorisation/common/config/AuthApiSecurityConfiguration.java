package recolnat.org.authorisation.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import recolnat.org.authorisation.common.exception.ErrorDetail;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity(debug = false)
@EnableMethodSecurity(jsr250Enabled = true, mode = AdviceMode.PROXY, prePostEnabled = true)
@RequiredArgsConstructor
public class AuthApiSecurityConfiguration {


    public static final String ROLE_PREFIX = "ROLE_";
    private final ObjectMapper objectMapper;
    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;

    /**
     * all calls HttpMethod.GET go through authenticated() method
     * if you don't specify the exact path, the RequestMatcherDelegatingAuthorizationManager class will handle calls to the /** expression
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(management -> management
                        .sessionCreationPolicy(STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/v1/users/{uid}/permissions").authenticated()
                        .requestMatchers("/v1/users/**", "/v1/roles/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui**", "/v3/api-docs/**", "/v3/api-docs**", "/actuator/**").permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(opaqueToken -> opaqueToken
                        .introspector(introspector())
                ))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), PATCH.name()));
        configuration.setAllowCredentials(false);
        configuration.setExposedHeaders(Arrays.asList("*"));
        //the below three lines will add the relevant CORS response headers
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            final var errorDetail = ErrorDetail.builder()
                    .message("Access Denied : you don't have a role for the action")
                    .status(HttpStatus.FORBIDDEN.value())
                    .code("ACCESS_DENIED_CODE")
                    .timestamp(LocalDateTime.now())
                    .detail(accessDeniedException.getMessage())
                    .developerMessage(AccessDeniedException.class.getCanonicalName()).build();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            OutputStream out = response.getOutputStream();
            objectMapper.writeValue(out, errorDetail);
            out.flush();
        };
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            final var errorDetail = ErrorDetail.builder()
                    .message("you are not authenticated")
                    .detail(authException.getMessage())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .code("UNAUTHORIZED_ACCESS_CODE")
                    .developerMessage(AccessDeniedException.class.getCanonicalName())
                    .timestamp(LocalDateTime.now()).build();


            OutputStream out = response.getOutputStream();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(out, errorDetail);
            out.flush();
        };
    }

    @Bean
    OpaqueTokenIntrospector introspector() {
        return new CustomAuthoritiesOpaqueTokenIntrospector(oAuth2ResourceServerProperties);
    }
}



