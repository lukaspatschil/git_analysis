package com.tuwien.gitanalyser.endpoints.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommitDTO {
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
