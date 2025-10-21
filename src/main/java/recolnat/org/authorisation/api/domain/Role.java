package recolnat.org.authorisation.api.domain;

import lombok.*;
import recolnat.org.authorisation.api.domain.enums.RoleEnum;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class Role {
	private String id;
	private String name;
	private String description;
	private RoleEnum code;
	private boolean assignable;
}
