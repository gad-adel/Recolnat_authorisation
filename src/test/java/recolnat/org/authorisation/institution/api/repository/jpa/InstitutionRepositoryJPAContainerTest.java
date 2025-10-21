package recolnat.org.authorisation.institution.api.repository.jpa;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import recolnat.org.authorisation.AbstractServiceTest;
import recolnat.org.authorisation.repository.jpa.InstitutionRepositoryJPA;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/***
 * test dans base postgresprofile= int (integration)
 */
@SpringBootTest
@ActiveProfiles("int")
class InstitutionRepositoryJPAContainerTest extends AbstractServiceTest {
    @Autowired
    private InstitutionRepositoryJPA repositoryJPA;

    @BeforeAll
    protected static void setup() {
        postgreSQLContainer.start();
    }

    @Test
    void test_get_all_inst_should_be_ok() {
        assertThat(repositoryJPA.findAll().toArray()).hasSizeLessThanOrEqualTo(87);
    }

}
