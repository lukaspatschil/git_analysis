package com.tuwien.gitanalyser.endpoints.DTOs.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NotSavedRepositoryInternalDTO {
    private Long platformId;
    private String name;
    private String url;
}
