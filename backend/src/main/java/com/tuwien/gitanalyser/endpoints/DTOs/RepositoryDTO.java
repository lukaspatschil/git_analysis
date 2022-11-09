package com.tuwien.gitanalyser.endpoints.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDTO {
    private Long id;
    private String name;
    private String url;
}
