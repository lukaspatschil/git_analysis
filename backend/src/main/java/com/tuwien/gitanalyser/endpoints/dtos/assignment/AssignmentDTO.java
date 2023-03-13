package com.tuwien.gitanalyser.endpoints.dtos.assignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AssignmentDTO {
    private String key;
    private List<SubAssignmentDTO> assignedNames;
}
