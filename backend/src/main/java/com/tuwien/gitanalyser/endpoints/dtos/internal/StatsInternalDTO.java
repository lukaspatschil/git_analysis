package com.tuwien.gitanalyser.endpoints.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsInternalDTO {
    private String committer;
    private int numberOfCommits;
    private int numberOfAdditions;
    private int numberOfDeletions;
}
