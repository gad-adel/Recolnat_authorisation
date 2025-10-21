package recolnat.org.authorisation.api.domain;


import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserProfile {

    private UUID uid;

    private String username;

    private String email;

    private String firstName;

    private String lastName;
    private Role role;
    private Institution institution;
    private Set<String> collections;

}
