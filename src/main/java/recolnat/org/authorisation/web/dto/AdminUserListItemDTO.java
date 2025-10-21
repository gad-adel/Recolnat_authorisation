package recolnat.org.authorisation.web.dto;

import java.util.UUID;

public record AdminUserListItemDTO(UUID id, String name, String institutionName, String role, String email) {
}
