package com.omar.infrastructure;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PostMapper {

  String getId(Long id);
}
