package com.tuwien.gitanalyser.endpoints.DTOs.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BranchInternalDTO {
    private String name;
}
