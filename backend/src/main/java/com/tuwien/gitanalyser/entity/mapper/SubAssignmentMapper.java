package com.tuwien.gitanalyser.entity.mapper;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.SubAssignmentDTO;
import com.tuwien.gitanalyser.entity.SubAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubAssignmentMapper {
    @Mapping(source = "assignedName", target = "name")
    SubAssignmentDTO entityToDTO(SubAssignment subAssignment);
}
