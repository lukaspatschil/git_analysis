package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.entity.Repository;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RepositoryMapper {
    RepositoryDTO entityToDTO(Repository repository);

    Repository dtoToEntity(RepositoryDTO repositoryDTO);
}
