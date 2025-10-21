package recolnat.org.authorisation.common.mapper;

import org.mapstruct.Mapper;
import recolnat.org.authorisation.connector.api.domain.OutputCollection;
import recolnat.org.authorisation.repository.entity.CollectionJPA;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    OutputCollection collectionJPAtoOutputCollection(CollectionJPA collectionJpa);
}
