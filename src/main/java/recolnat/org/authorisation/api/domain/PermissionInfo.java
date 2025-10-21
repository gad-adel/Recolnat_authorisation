package recolnat.org.authorisation.api.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import recolnat.org.authorisation.service.CollectionCheck;
import recolnat.org.authorisation.service.InstitutionCheck;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.util.StreamUtils.toUnmodifiableSet;

@Data
@Builder
public class PermissionInfo {
    @NotNull(message = "User id s required")
    private UUID userId;
    @NotNull(message = "Role id s required")
    private UUID roleId;
    @NotNull(groups = {InstitutionCheck.class, CollectionCheck.class}, message = "institution Id is required")
    private UUID institutionId;
    @NotEmpty(groups = {CollectionCheck.class}, message = "list of collectionId are required")
    private List<UUID> collections;

    public List<String> toCollectionsAsString() {
        return this.collections.stream().map(UUID::toString).collect(toUnmodifiableSet()).stream().toList();
    }
}
