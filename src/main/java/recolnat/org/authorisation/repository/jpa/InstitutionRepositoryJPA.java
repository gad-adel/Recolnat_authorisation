package recolnat.org.authorisation.repository.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import recolnat.org.authorisation.repository.entity.InstitutionJPA;

import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepositoryJPA extends JpaRepository<InstitutionJPA, Integer> {

    Optional<InstitutionJPA> findByInstitutionId(UUID uuid);
}
