package utils;

import com.tuwien.gitanalyser.endpoints.dtos.BranchDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitterDTO;
import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.dtos.StatsDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.AssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.SubAssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitAggregatedInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.SubAssignment;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;

public class Matchers {

    public static Matcher<SubAssignmentDTO> subAssignmentDTOMatcher(SubAssignment subAssignment12) {
        return allOf(hasFeature("name", SubAssignmentDTO::getName, equalTo(subAssignment12.getAssignedName())),
                     hasFeature("id", SubAssignmentDTO::getId, equalTo(subAssignment12.getId())));
    }

    public static Matcher<StatsInternalDTO> statsInternalDTOMatcher(StatsInternalDTO resultStats) {
        return allOf(hasFeature("committer", StatsInternalDTO::getCommitter, equalTo(resultStats.getCommitter())),
                     hasFeature("numberOfCommits",
                                StatsInternalDTO::getNumberOfCommits,
                                equalTo(resultStats.getNumberOfCommits())),
                     hasFeature("numberOfAdditions",
                                StatsInternalDTO::getNumberOfAdditions,
                                equalTo(resultStats.getNumberOfAdditions())),
                     hasFeature("numberOfDeletions",
                                StatsInternalDTO::getNumberOfDeletions,
                                equalTo(resultStats.getNumberOfDeletions())));
    }

    public static Matcher<NotSavedRepositoryDTO> repositoryMatcher(Project project) {
        return allOf(hasFeature("id", NotSavedRepositoryDTO::getId, equalTo(project.getId())),
                     hasFeature("name", NotSavedRepositoryDTO::getName, equalTo(project.getName())),
                     hasFeature("url", NotSavedRepositoryDTO::getUrl, equalTo(project.getHttpUrlToRepo())));
    }

    public static Matcher<StatsDTO> statsDTOMatcher(Commit commit) {
        return allOf(hasFeature("additions", StatsDTO::getNumberOfAdditions, equalTo(commit.getStats().getAdditions())),
                     hasFeature("deletions", StatsDTO::getNumberOfDeletions, equalTo(commit.getStats().getDeletions())),
                     hasFeature("committer", StatsDTO::getCommitter, equalTo(commit.getAuthorName())),
                     hasFeature("committer", StatsDTO::getNumberOfCommits, equalTo(1)));
    }

    public static Matcher<SubAssignment> subAssignmentMatcher(SubAssignment subAssignment2) {
        return allOf(hasFeature("id", SubAssignment::getId, equalTo(subAssignment2.getId())),
                     hasFeature("name", SubAssignment::getAssignedName, equalTo(subAssignment2.getAssignedName())));
    }

    public static Matcher<AssignmentDTO> assignmentMatcher(String key1, SubAssignment subAssignment21,
                                                           SubAssignment subAssignment22) {
        return allOf(hasFeature("key", AssignmentDTO::getKey, equalTo(key1)),
                     hasFeature("subAssignment",
                                AssignmentDTO::getAssignedNames,
                                containsInAnyOrder(subAssignmentDTOMatcher(subAssignment21),
                                                   subAssignmentDTOMatcher(subAssignment22))));
    }

    public static Matcher<CommitAggregatedInternalDTO> commitAggreagteDTOMatcher(CommitInternalDTO commit,
                                                                                 int linesOfCodeOverall) {
        return allOf(hasFeature("id", CommitAggregatedInternalDTO::getId, equalTo(commit.getId())),
                     hasFeature("message", CommitAggregatedInternalDTO::getMessage, equalTo(commit.getMessage())),
                     hasFeature("author", CommitAggregatedInternalDTO::getAuthor, equalTo(commit.getAuthor())),
                     hasFeature("timestamp", CommitAggregatedInternalDTO::getTimestamp, equalTo(commit.getTimestamp())),
                     hasFeature("additions", CommitAggregatedInternalDTO::getAdditions, equalTo(commit.getAdditions())),
                     hasFeature("deletions", CommitAggregatedInternalDTO::getDeletions, equalTo(commit.getDeletions())),
                     hasFeature("linesOfCodeOverall",
                                CommitAggregatedInternalDTO::getLinesOfCodeOverall,
                                equalTo(linesOfCodeOverall)),
                     hasFeature("isMergeCommit",
                                CommitAggregatedInternalDTO::isMergeCommit,
                                equalTo(commit.isMergeCommit())),
                     hasFeature("parentIds",
                                CommitAggregatedInternalDTO::getParentIds,
                                equalTo(commit.getParentIds())));
    }

    public static Matcher<CommitDTO> commitDTOMatcher(CommitDTO commitDTO) {
        return allOf(hasFeature("id", CommitDTO::getId, equalTo(commitDTO.getId())),
                     hasFeature("message", CommitDTO::getMessage, equalTo(commitDTO.getMessage())),
                     hasFeature("author", CommitDTO::getAuthor, equalTo(commitDTO.getAuthor())),
                     hasFeature("timestamp", CommitDTO::getTimestamp, equalTo(commitDTO.getTimestamp())),
                     hasFeature("parentIds", CommitDTO::getParentIds, equalTo(commitDTO.getParentIds())),
                     hasFeature("additions", CommitDTO::getAdditions, equalTo(commitDTO.getAdditions())),
                     hasFeature("deletions", CommitDTO::getDeletions, equalTo(commitDTO.getDeletions())));
    }

    public static Matcher<CommitDTO> commitDTOMatcher(CommitDTO commitDTO, int linesOfCode) {
        return allOf(hasFeature("id", CommitDTO::getId, equalTo(commitDTO.getId())),
                     hasFeature("message", CommitDTO::getMessage, equalTo(commitDTO.getMessage())),
                     hasFeature("author", CommitDTO::getAuthor, equalTo(commitDTO.getAuthor())),
                     hasFeature("timestamp", CommitDTO::getTimestamp, equalTo(commitDTO.getTimestamp())),
                     hasFeature("parentIds", CommitDTO::getParentIds, equalTo(commitDTO.getParentIds())),
                     hasFeature("additions", CommitDTO::getAdditions, equalTo(commitDTO.getAdditions())),
                     hasFeature("deletions", CommitDTO::getDeletions, equalTo(commitDTO.getDeletions())),
                     hasFeature("linesOfCode", CommitDTO::getLinesOfCodeOverall, equalTo(linesOfCode)));
    }

    public static Matcher<BranchDTO> branchDTOMatcher(Branch branch1) {
        return allOf(hasFeature("name", BranchDTO::getName, equalTo(branch1.getName())));
    }

    public static Matcher<CommitterDTO> committerDTOMatcher(CommitterDTO committerDTO) {
        return hasFeature("name", CommitterDTO::getName, equalTo(committerDTO.getName()));
    }

    public static Matcher<CommitterDTO> committerDTOMatcher(String name) {
        return hasFeature("name", CommitterDTO::getName, equalTo(name));
    }

    public static Matcher<StatsDTO> statsDTOMatcher(Commit... commits) {
        int additions = 0;
        int deletions = 0;

        for (Commit commit : commits) {
            additions += commit.getStats().getAdditions();
            deletions += commit.getStats().getDeletions();
        }

        return allOf(hasFeature("additions", StatsDTO::getNumberOfAdditions, equalTo(additions)),
                     hasFeature("deletions", StatsDTO::getNumberOfDeletions, equalTo(deletions)),
                     hasFeature("committer", StatsDTO::getCommitter, equalTo(commits[0].getAuthorName())),
                     hasFeature("committer", StatsDTO::getNumberOfCommits, equalTo(commits.length)));
    }

    public static Matcher<StatsDTO> statsDTOMatcher(String committerName, Commit... commits) {
        int additions = 0;
        int deletions = 0;

        for (Commit commit : commits) {
            additions += commit.getStats().getAdditions();
            deletions += commit.getStats().getDeletions();
        }

        return allOf(hasFeature("additions", StatsDTO::getNumberOfAdditions, equalTo(additions)),
                     hasFeature("deletions", StatsDTO::getNumberOfDeletions, equalTo(deletions)),
                     hasFeature("committer", StatsDTO::getCommitter, equalTo(committerName)),
                     hasFeature("committer", StatsDTO::getNumberOfCommits, equalTo(commits.length)));
    }

    public static Matcher<BranchInternalDTO> branchInternalDTOMatcher(Branch branchMock) {
        return hasFeature("name", BranchInternalDTO::getName, equalTo(branchMock.getName()));
    }

    public static Matcher<CommitterInternalDTO> committerInteralDTOMatcher(String author) {
        return hasFeature("name", CommitterInternalDTO::getName, equalTo(author));
    }
}
