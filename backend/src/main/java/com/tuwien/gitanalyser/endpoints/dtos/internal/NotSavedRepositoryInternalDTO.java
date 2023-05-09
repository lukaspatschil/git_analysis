package com.tuwien.gitanalyser.endpoints.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class NotSavedRepositoryInternalDTO {
    private Long platformId;
    private String name;
    private String url;
}
