package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.entity.SavedRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RepositoryMapper {
    @Mapping(source = "platformId", target = "id")
    RepositoryDTO entityToDTO(SavedRepository savedRepository);
}
