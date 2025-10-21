package recolnat.org.authorisation.web.dto;

import java.util.List;

public record AdminUserPageResponseDTO(Integer totalPages, Long numberOfElements, List<AdminUserListItemDTO> users) {
}
