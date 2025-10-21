package recolnat.org.authorisation.institution.api.repository.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import recolnat.org.authorisation.common.mapper.CollectionMapper;
import recolnat.org.authorisation.connector.api.domain.OutputCollection;
import recolnat.org.authorisation.repository.jpa.CollectionJPARepository;
import recolnat.org.authorisation.repository.jpa.InstitutionRepositoryJPA;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * test dans Base H2 . profile test
 */
@DataJpaTest
@ActiveProfiles("test")
class InstitutionRepositoryJPATest {

    private static CollectionMapper collectionMapper;
    @Autowired
    private InstitutionRepositoryJPA institutionRepositoryJPA;
    @Autowired
    private CollectionJPARepository collectionJPARepository;

    @BeforeAll
    static void init() {
        collectionMapper = Mappers.getMapper(CollectionMapper.class);
    }

    @Test
    @DisplayName("Retrieve all Intitution by pagination")
    @Sql(scripts = "classpath:init_ref_institution.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:init_ref_collections.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:clean_institution_collection.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void find_all_by_page() {
        var resp = institutionRepositoryJPA.findAll(PageRequest.of(0, 10));
        assertThat(resp.getTotalPages()).isEqualTo(9);
        assertThat(resp.getTotalElements()).isGreaterThanOrEqualTo(86);

        List<OutputCollection> listCollectionJpa = collectionJPARepository.findCollectionsByInstitutionId(resp.toList().get(0).getId()).stream()
                .map(collectionMapper::collectionJPAtoOutputCollection).toList();
        Assertions.assertNotNull(listCollectionJpa);
        assertEquals(8, listCollectionJpa.size());
    }

}
