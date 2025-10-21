package recolnat.org.authorisation.repository.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import recolnat.org.authorisation.repository.entity.CollectionJPA;

import java.util.List;
import java.util.UUID;

public interface CollectionJPARepository extends JpaRepository<CollectionJPA, UUID>{

	List<CollectionJPA> findCollectionsByInstitutionId(Integer institutionId);
	
}
