package recolnat.org.authorisation.common.mapper;

import io.recolnat.model.RoleResponseDTO;
import recolnat.org.authorisation.api.domain.Role;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
	
	RoleResponseDTO roleToRoleDto(Role role);
}
