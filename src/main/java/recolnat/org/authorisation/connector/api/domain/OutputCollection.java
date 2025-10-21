package recolnat.org.authorisation.connector.api.domain;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class OutputCollection {
    private UUID id;
	private String typeCollection;
	private String collectionNameFr;
	private String collectionNameEn;
	private String descriptionFr;
	private String descriptionEn;
}
