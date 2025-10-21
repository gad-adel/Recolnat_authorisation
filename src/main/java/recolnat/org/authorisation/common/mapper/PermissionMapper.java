package recolnat.org.authorisation.common.mapper;

import io.recolnat.model.PermissionRequestDTO;
import recolnat.org.authorisation.api.domain.PermissionInfo;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
	
	@Mapping(target = "userId", ignore = true)
    PermissionInfo toPermissionInfo(PermissionRequestDTO requestDTO);
}
