package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.dtos.CommitterDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CommitterMapper {
    CommitterDTO dtoToDTO(CommitterInternalDTO commit);

    List<CommitterDTO> dtosToDTOs(Set<CommitterInternalDTO> commitInternalDTOS);
}

