package recolnat.org.authorisation.repository.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Entity(name = "collection")
public class CollectionJPA extends AbstractEntity<UUID> {
	
	private String typeCollection;
	private String collectionNameFr;
	private String collectionNameEn;
	private String descriptionFr;
	private String descriptionEn;
	private Integer institutionId;
	private LocalDateTime dataChangeTs;

}
