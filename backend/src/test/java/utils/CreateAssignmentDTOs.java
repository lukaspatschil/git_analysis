package utils;

import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;

public class CreateAssignmentDTOs {
    public static CreateAssignmentDTO random() {
        return CreateAssignmentDTO.builder().assignedName(Randoms.alpha()).key(Randoms.alpha()).build();
    }
}
