package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommitMapper {
    @Mapping(source = "mergeCommit", target = "isMergeCommit")
    CommitDTO dtoToDTO(CommitInternalDTO commit);

    List<CommitDTO> dtosToDTOs(List<CommitInternalDTO> commitInternalDTOS);
}

