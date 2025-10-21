package recolnat.org.authorisation.common.mapper;

import io.recolnat.model.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import recolnat.org.authorisation.api.domain.UserProfile;
import recolnat.org.authorisation.api.domain.UserProfilePage;
import recolnat.org.authorisation.web.dto.AdminUserListItemDTO;
import recolnat.org.authorisation.web.dto.AdminUserPageResponseDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(UserProfile userProfile);

    @Mapping(target = "id", source = "uid")
    @Mapping(target = "name", expression = "java(userProfile.getLastName() + \" \" + userProfile.getFirstName())")
    @Mapping(target = "institutionName", source = "institution.name")
    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "email", source = "email")
    AdminUserListItemDTO toAdminDto(UserProfile userProfile);

    AdminUserPageResponseDTO toAdminResponseDTO(UserProfilePage userProfilePage);
}
