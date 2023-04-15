package com.tuwien.gitanalyser.endpoints.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StatsDTO {
    private String committer;
    private int numberOfCommits;
    private int numberOfAdditions;
    private int numberOfDeletions;
}
