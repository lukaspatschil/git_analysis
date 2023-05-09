package com.tuwien.gitanalyser.endpoints.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class CommitAggregatedInternalDTO {
    private String id;
    private String message;
    private String author;
    private Date timestamp;
    private List<String> parentIds;
    private boolean isMergeCommit;
    private Integer additions;
    private Integer deletions;
    private Integer linesOfCodeOverall;
}
