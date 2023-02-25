package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotSavedRepositoryMapper {
    @Mapping(source = "platformId", target = "id")
    NotSavedRepositoryDTO dtoToDTO(NotSavedRepositoryInternalDTO notSavedRepositoryInternalDTO);

    List<NotSavedRepositoryDTO> dtosToDTOs(List<NotSavedRepositoryInternalDTO> notSavedRepositoryInternalDTOs);
}
