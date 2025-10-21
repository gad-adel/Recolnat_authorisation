package recolnat.org.authorisation.connector.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;
import recolnat.org.authorisation.common.exception.AuthorisationTechnicalException;
import recolnat.org.authorisation.common.mapper.CollectionMapper;
import recolnat.org.authorisation.connector.api.CollectionApiClient;
import recolnat.org.authorisation.connector.api.domain.OutputCollection;
import recolnat.org.authorisation.repository.jpa.CollectionJPARepository;
import recolnat.org.authorisation.service.TokenService;

import static java.util.Objects.isNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionApiClientImpl implements CollectionApiClient {

	private final WebClient webClient;
	private final TokenService tokenService;
	private final CollectionJPARepository collectionJPARepository;
	private final CollectionMapper collectionMapper;
	
	@Override
	public List<OutputCollection> retrieveCollectionsByInstitutions(int id) {
		List<OutputCollection> list=findByJpa(id);
		if(isNull(list) || list.isEmpty()) {
			list=findByWebClient(id);
		}
	  return list;
	}
	
	
	private List<OutputCollection> findByJpa(int id) {
		try {
			return collectionJPARepository.findCollectionsByInstitutionId(id).stream()
				.map(collectionMapper::collectionJPAtoOutputCollection).toList();
		}catch(Exception e) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * emploi d un client WebClient appelant collection manager
	 * @param id
	 * @return
	 */
	private List<OutputCollection> findByWebClient(int id) {
		var jwtToken = tokenService.getJwtToken()
				.orElseThrow(() -> new AuthorisationBusinessException(HttpStatus.NOT_FOUND, 
						"error.jwt.token", "Cannot get Jwt Token"));
	    Mono<List<OutputCollection>> listAllCollection =
	        webClient
	            .get()
	            .uri("/v1/institutions/" + id + "/collections")
	            .headers(h -> h.setBearerAuth(jwtToken))
	            .retrieve()
	            .onStatus(
	                HttpStatusCode::isError,
	                response -> {
	                  log.error(
	                      "Error retreiveCollectionsByInstitutions Client {}", response.statusCode());
	                  throw new AuthorisationTechnicalException(
	                      "SERVER_ERROR_CODE",
	                      "error.collection.manager",
	                      HttpStatus.SERVICE_UNAVAILABLE.value(),
	                      "We can't get resources on collection-manager API");
	                })
	            .bodyToMono(new ParameterizedTypeReference<>() {});
			return listAllCollection.block();
	}

}
