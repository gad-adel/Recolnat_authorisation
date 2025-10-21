package recolnat.org.authorisation.common.mapper;



import static java.util.Objects.isNull;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import recolnat.org.authorisation.api.domain.Institution;
import recolnat.org.authorisation.api.domain.enums.PartnerType;
import recolnat.org.authorisation.repository.entity.InstitutionJPA;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {
	

	/**
	 * mapper employe pour buid institution avec assignable=true
	 * @param inst
	 * @return
	 */
    @Mapping(target = "partnerTypeEn", source = "partnerType", qualifiedByName = "PartnerTypeEn")
    @Mapping(target = "partnerTypeFr", source = "partnerType", qualifiedByName = "PartnerTypeFr")
    @Mapping(target = "assignable", constant = "true")
    Institution toInstitution(InstitutionJPA inst);


    
    @Named("PartnerTypeEn")
    default String mapPartnerTypeEn(PartnerType partnerType) {
        return isNull(partnerType) ? null : partnerType.getPartnerEn();
    }
    
    @Named("PartnerTypeFr")
    default String mapPartnerTypeFr(PartnerType partnerType) {
        return isNull(partnerType) ? null : partnerType.getPartnerFr();
    }

}
