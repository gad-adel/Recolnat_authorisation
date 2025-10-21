package recolnat.org.authorisation.api.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class UserProfilePage {

    private Integer totalPages;

    private long numberOfElements;

    private List<UserProfile> users;
}
