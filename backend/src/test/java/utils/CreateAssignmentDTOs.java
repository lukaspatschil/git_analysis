package utils;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;

import java.util.List;

public class CreateAssignmentDTOs {
    public static CreateAssignmentDTO random() {
        return CreateAssignmentDTO.builder().assignedNames(List.of(Randoms.alpha())).key(Randoms.alpha()).build();
    }
}
