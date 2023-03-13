package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.AssignmentDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SubAssignmentMapper.class})
public interface AssignmentMapper {

    List<AssignmentDTO> entitiesToDTO(List<Assignment> assignments);

    @Mapping(source = "subAssignments", target = "assignedNames")
    AssignmentDTO entityToDTO(Assignment assignment);
}
