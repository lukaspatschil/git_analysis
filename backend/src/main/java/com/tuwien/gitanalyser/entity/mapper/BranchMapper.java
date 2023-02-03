package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.DTOs.BranchDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.internal.BranchInternalDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BranchMapper {
    BranchDTO dtoToDTO(BranchInternalDTO notSavedRepositoryInternalDTO);

    List<BranchDTO> dtosToDTOs(List<BranchInternalDTO> notSavedRepositoryInternalDTOs);
}
