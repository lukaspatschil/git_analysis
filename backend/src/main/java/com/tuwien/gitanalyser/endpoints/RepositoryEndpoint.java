package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.dtos.BranchDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitterDTO;
import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.AssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.entity.mapper.AssignmentMapper;
import com.tuwien.gitanalyser.entity.mapper.BranchMapper;
import com.tuwien.gitanalyser.entity.mapper.CommitMapper;
import com.tuwien.gitanalyser.entity.mapper.CommitterMapper;
import com.tuwien.gitanalyser.entity.mapper.NotSavedRepositoryMapper;
import com.tuwien.gitanalyser.security.SecurityAnnotations;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/apiV1/repository")
public class RepositoryEndpoint extends BaseEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryEndpoint.class);
    private final RepositoryService repositoryService;
    private final GitService gitService;
    private final NotSavedRepositoryMapper notSavedRepositoryMapper;
    private final BranchMapper branchMapper;
    private final CommitMapper commitsMapper;
    private final CommitterMapper committerMapper;

    private final AssignmentMapper assignmentMapper;

    public RepositoryEndpoint(final RepositoryService repositoryService,
                              final GitService gitService,
                              final NotSavedRepositoryMapper notSavedRepositoryMapper,
                              final BranchMapper branchMapper,
                              final CommitMapper commitMapper,
                              final CommitterMapper committerMapper,
                              final AssignmentMapper assignmentMapper) {
        this.repositoryService = repositoryService;
        this.gitService = gitService;
        this.notSavedRepositoryMapper = notSavedRepositoryMapper;
        this.branchMapper = branchMapper;
        this.commitsMapper = commitMapper;
        this.committerMapper = committerMapper;
        this.assignmentMapper = assignmentMapper;
    }

    @GetMapping
    public List<NotSavedRepositoryDTO> getAllRepositories(final Authentication authentication) {
        LOGGER.info("GET /repository - get all repositories");
        return notSavedRepositoryMapper.dtosToDTOs(
            gitService.getAllRepositories(getUserId(authentication))
        );
    }

    @GetMapping("/{platformId}")
    public NotSavedRepositoryDTO getRepositoryById(
        final Authentication authentication,
        final @PathVariable Long platformId) {
        LOGGER.info("GET /repository/{id} - get repository by platform id {}", platformId);
        try {
            return notSavedRepositoryMapper.dtoToDTO(
                gitService.getRepositoryById(getUserId(authentication), platformId)
            );
        } catch (GitLabApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{platformId}/branch")
    public List<BranchDTO> getBranchesByRepositoryId(
        final Authentication authentication,
        final @PathVariable Long platformId) {
        LOGGER.info("GET /repository/{id}/branch - get repository by platform id {}", platformId);
        try {
            return branchMapper.dtosToDTOs(gitService.getAllBranches(getUserId(authentication), platformId));
        } catch (GitLabApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{platformId}/commit")
    public List<CommitDTO> getCommitsByRepositoryId(
        final Authentication authentication,
        final @PathVariable Long platformId,
        final @RequestParam(name = "branch", required = false) String branch) {
        LOGGER.info("GET /repository/{id}/commits - get repository by platform id {} and branch {}",
                    platformId,
                    branch);
        try {
            return commitsMapper.dtosToDTOs(gitService.getAllCommits(getUserId(authentication),
                                                                     platformId, branch));
        } catch (GitLabApiException | IOException e) {
            LOGGER.error("Error while getting commits", e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{platformId}/committer")
    public List<CommitterDTO> getCommittersByRepositoryId(
        final Authentication authentication,
        final @PathVariable Long platformId,
        final @RequestParam(name = "branch", required = false) String branch) {
        LOGGER.info("GET /repository/{id}/committers - get repository by platform id {} and branch {}",
                    platformId, branch);
        try {
            return committerMapper.dtosToDTOs(gitService.getAllCommitters(getUserId(authentication),
                                                                          platformId, branch));
        } catch (GitLabApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{platformId}/assignment")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityAnnotations.UserOwnsRepo()
    public void assignCommitters(final Authentication authentication,
                                 final @PathVariable Long platformId,
                                 final @RequestBody CreateAssignmentDTO createAssignmentDTO) {
        LOGGER.info("POST /repository/{id}/assignment - assign committer to repository by platform id {} "
                        + "values {}", platformId, createAssignmentDTO.toString());

        repositoryService.assignCommitter(getUserId(authentication), platformId,
                                          createAssignmentDTO);
    }

    @GetMapping("/{platformId}/assignment")
    @SecurityAnnotations.UserOwnsRepo()
    public List<AssignmentDTO> getAssignments(final Authentication authentication,
                                              final @PathVariable Long platformId) {
        LOGGER.info("GET /repository/{id}/assignment - get assignments from repository by platform id {} ", platformId);

        return assignmentMapper.entitiesToDTO(repositoryService.getAssignments(getUserId(authentication),
                                                                               platformId));
    }

    @DeleteMapping("/{platformId}/assignment/{subAssignmentId}")
    @SecurityAnnotations.UserOwnsRepo
    public void deleteAssignment(final Authentication authentication,
                                 final @PathVariable("platformId") Long platformId,
                                 final @PathVariable("subAssignmentId") Long subAssignmentId) {
        LOGGER.info("DELETE /repository/{id}/assignment/{subAssignmentId} - delete assignment from repository "
                        + "by platform id {} and subAssignmentId {}",
                    platformId,
                    subAssignmentId);

        repositoryService.deleteAssignment(getUserId(authentication), platformId, subAssignmentId);
    }
}
