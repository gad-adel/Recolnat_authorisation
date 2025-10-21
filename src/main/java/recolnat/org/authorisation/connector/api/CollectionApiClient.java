package recolnat.org.authorisation.connector.api;

import java.util.List;

import recolnat.org.authorisation.connector.api.domain.OutputCollection;


public interface CollectionApiClient {

	List<OutputCollection> retrieveCollectionsByInstitutions(int id);
}
