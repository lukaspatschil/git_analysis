package com.tuwien.gitanalyser.endpoints.dtos.assignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubAssignmentDTO {
    private Long id;
    private String name;
}
