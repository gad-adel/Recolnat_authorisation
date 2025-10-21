package recolnat.org.authorisation.api.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class CollectionsInstitution {

	private String typeCollection;
	private String collectionCode;
}
