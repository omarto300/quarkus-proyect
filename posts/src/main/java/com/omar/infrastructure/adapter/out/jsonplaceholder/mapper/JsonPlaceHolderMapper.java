package com.omar.infrastructure.adapter.out.jsonplaceholder.mapper;

import com.omar.domain.model.Autor;
import com.omar.domain.model.Post;
import com.omar.infrastructure.adapter.out.jsonplaceholder.dto.JphPostDto;
import com.omar.infrastructure.adapter.out.jsonplaceholder.dto.JphUserDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface JsonPlaceHolderMapper {

  @Mapping(source = "userId", target = "idAutor")
  @Mapping(source = "title", target = "titulo")
  @Mapping(source = "body", target = "cuerpo")
  Post toDomain(JphPostDto jPostDto);

  List<Post> toDomainList(List<JphPostDto> dtos);

  @Mapping(source = "name", target = "nombre")
  Autor toDomain(JphUserDto userDto);
}
