package recolnat.org.authorisation.repository.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import recolnat.org.authorisation.api.domain.PartnerTypeConverter;
import recolnat.org.authorisation.api.domain.enums.PartnerType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name="institution")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@SuperBuilder
public class InstitutionJPA{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private UUID institutionId;
    private String code;
    private String name;
    private String mandatoryDescription;
    private String optionalDescription;
    @Convert(converter = PartnerTypeConverter.class)
    private PartnerType partnerType;
    private String logoUrl;
    private LocalDateTime createdAt;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime modifiedAt;
    private LocalDateTime dataChangeTs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        InstitutionJPA that = (InstitutionJPA) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
