package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.dtos.BranchDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitterDTO;
import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.dtos.StatsDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.AssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.assignment.CreateAssignmentDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.BranchInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitAggregatedInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.CommitterInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.NotSavedRepositoryInternalDTO;
import com.tuwien.gitanalyser.endpoints.dtos.internal.StatsInternalDTO;
import com.tuwien.gitanalyser.entity.Assignment;
import com.tuwien.gitanalyser.entity.mapper.AssignmentMapper;
import com.tuwien.gitanalyser.entity.mapper.BranchMapper;
import com.tuwien.gitanalyser.entity.mapper.CommitMapper;
import com.tuwien.gitanalyser.entity.mapper.CommitterMapper;
import com.tuwien.gitanalyser.entity.mapper.NotSavedRepositoryMapper;
import com.tuwien.gitanalyser.entity.mapper.StatsMapper;
import com.tuwien.gitanalyser.exception.BadRequestException;
import com.tuwien.gitanalyser.exception.ConflictException;
import com.tuwien.gitanalyser.exception.GitException;
import com.tuwien.gitanalyser.exception.IllegalArgumentException;
import com.tuwien.gitanalyser.exception.InternalServerErrorException;
import com.tuwien.gitanalyser.exception.NoProviderFoundException;
import com.tuwien.gitanalyser.security.SecurityAnnotations;
import com.tuwien.gitanalyser.service.GitService;
import com.tuwien.gitanalyser.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;
import java.util.Set;

@RestController()
@RequestMapping("/apiV1/repository")
@SecurityRequirement(name = "oauth2")
@Tag(name = "Repository Endpoint")
public class RepositoryEndpoint extends BaseEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryEndpoint.class);
    private final RepositoryService repositoryService;
    private final GitService gitService;
    private final NotSavedRepositoryMapper notSavedRepositoryMapper;
    private final BranchMapper branchMapper;
    private final CommitMapper commitsMapper;
    private final CommitterMapper committerMapper;
    private final AssignmentMapper assignmentMapper;
    private final StatsMapper statsMapper;

    public RepositoryEndpoint(final RepositoryService repositoryService,
                              final GitService gitService,
                              final NotSavedRepositoryMapper notSavedRepositoryMapper,
                              final BranchMapper branchMapper,
                              final CommitMapper commitMapper,
                              final CommitterMapper committerMapper,
                              final AssignmentMapper assignmentMapper,
                              final StatsMapper statsMapper) {
        this.repositoryService = repositoryService;
        this.gitService = gitService;
        this.notSavedRepositoryMapper = notSavedRepositoryMapper;
        this.branchMapper = branchMapper;
        this.commitsMapper = commitMapper;
        this.committerMapper = committerMapper;
        this.assignmentMapper = assignmentMapper;
        this.statsMapper = statsMapper;
    }

    @GetMapping
    @Operation(description = "Get all repositories", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = NotSavedRepositoryDTO.class)),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public List<NotSavedRepositoryDTO> getAllRepositories(final Authentication authentication)
        throws InternalServerErrorException, BadRequestException {
        LOGGER.info("GET /repository - get all repositories");
        List<NotSavedRepositoryInternalDTO> repositories;
        try {
            repositories = gitService.getAllRepositories(getUserId(authentication));
        } catch (NoProviderFoundException e) {
            throw new InternalServerErrorException();
        } catch (GitException e) {
            throw new BadRequestException();
        }
        return notSavedRepositoryMapper.dtosToDTOs(repositories);
    }

    @GetMapping("/{platformId}")
    @Operation(description = "Get repository by id", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            schema = @Schema(implementation = NotSavedRepositoryDTO.class),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public NotSavedRepositoryDTO getRepositoryById(
        final Authentication authentication,
        final @PathVariable Long platformId) throws InternalServerErrorException, BadRequestException {
        LOGGER.info("GET /repository/{id} - get repository by platform id {}", platformId);

        NotSavedRepositoryInternalDTO repository;
        try {
            repository = gitService.getRepositoryById(getUserId(authentication),
                                                      platformId);
        } catch (NoProviderFoundException e) {
            throw new InternalServerErrorException();
        } catch (GitException e) {
            throw new BadRequestException();
        }
        return notSavedRepositoryMapper.dtoToDTO(repository);
    }

    @GetMapping("/{platformId}/branch")
    @Operation(description = "Get all branches for a repository", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = BranchDTO.class)),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public List<BranchDTO> getBranchesByRepositoryId(
        final Authentication authentication,
        final @PathVariable Long platformId) throws InternalServerErrorException, BadRequestException {
        LOGGER.info("GET /repository/{id}/branch - get repository by platform id {}", platformId);
        List<BranchInternalDTO> branches;
        try {
            branches = gitService.getAllBranches(getUserId(authentication), platformId);
        } catch (NoProviderFoundException e) {
            throw new InternalServerErrorException();
        } catch (GitException e) {
            throw new BadRequestException();
        }
        return branchMapper.dtosToDTOs(branches);
    }

    @GetMapping("/{platformId}/commit")
    @Operation(description = "Get all branches for a repository", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = CommitDTO.class)),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public List<CommitDTO> getCommitsByRepositoryId(
        final Authentication authentication,
        final @PathVariable Long platformId,
        final @RequestParam(name = "branch", required = false) String branch,
        final @RequestParam(name = "mappedByAssignments", required = false,
            defaultValue = "false") Boolean mappedByAssignments,
        final @RequestParam(name = "committerName", required = false) String name)
        throws BadRequestException, InternalServerErrorException {
        LOGGER.info("GET /repository/{id}/commits - get repository by platform id {} and branch {}",
                    platformId, branch);
        List<CommitAggregatedInternalDTO> commits;
        try {
            commits = repositoryService.getCommits(getUserId(authentication),
                                                   platformId, branch, mappedByAssignments, name);
        } catch (NoProviderFoundException e) {
            throw new InternalServerErrorException();
        } catch (GitException e) {
            throw new BadRequestException();
        }
        return commitsMapper.dtosToDTOs(commits);
    }

    @GetMapping("/{platformId}/committer")
    @Operation(description = "Get all committers for a repository", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = CommitterDTO.class)),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public List<CommitterDTO> getCommittersByRepositoryId(
        final Authentication authentication,
        final @PathVariable Long platformId,
        final @RequestParam(name = "branch", required = false) String branch,
        final @RequestParam(name = "mappedByAssignments", required = false,
            defaultValue = "false") Boolean mappedByAssignments)
        throws InternalServerErrorException, BadRequestException {
        LOGGER.info("GET /repository/{id}/committers - get repository by platform id {} and branch {}",
                    platformId, branch);
        Set<CommitterInternalDTO> committers;
        try {
            committers = repositoryService.getCommitters(getUserId(authentication), platformId,
                                                         branch, mappedByAssignments);
        } catch (NoProviderFoundException e) {
            throw new InternalServerErrorException();
        } catch (GitException e) {
            throw new BadRequestException();
        }
        return committerMapper.dtosToDTOs(committers);
    }

    @PostMapping("/{platformId}/assignment")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityAnnotations.UserOwnsRepo()
    @Operation(description = "Add assignment for committers for a repository", responses = {
        @ApiResponse(responseCode = "201"),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public void assignCommitters(final Authentication authentication,
                                 final @PathVariable Long platformId,
                                 final @RequestBody CreateAssignmentDTO createAssignmentDTO) {
        LOGGER.info("POST /repository/{id}/assignment - assign committer to repository by platform id {} "
                        + "values {}", platformId, createAssignmentDTO.toString());

        try {
            repositoryService.addAssignment(getUserId(authentication), platformId,
                                            createAssignmentDTO);
        } catch (IllegalArgumentException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @GetMapping("/{platformId}/assignment")
    @SecurityAnnotations.UserOwnsRepo()
    @Operation(description = "Get assignment for committers for a repository", responses = {
        @ApiResponse(responseCode = "201", content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = AssignmentDTO.class)),
            mediaType = "application/json"))
    })
    public List<AssignmentDTO> getAssignments(final Authentication authentication,
                                              final @PathVariable Long platformId) {
        LOGGER.info("GET /repository/{id}/assignment - get assignments from repository by platform id {} ", platformId);

        List<Assignment> assignments = repositoryService.getAssignments(getUserId(authentication),
                                                                        platformId);
        return assignmentMapper.entitiesToDTO(assignments);
    }

    @GetMapping("/{platformId}/stats")
    @Operation(description = "Get statistics for repository and branch", responses = {
        @ApiResponse(responseCode = "200", content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = StatsDTO.class)),
            mediaType = "application/json")),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public List<StatsDTO> getStats(final Authentication authentication,
                                   final @PathVariable Long platformId,
                                   final @RequestParam(name = "branch", required = false) String branch,
                                   final @RequestParam(name = "mappedByAssignments", required = false,
                                       defaultValue = "false") Boolean mappedByAssignments)
        throws InternalServerErrorException, BadRequestException {
        LOGGER.info("GET /repository/{id}/stats - get statistics from repository by platform id {} and branch {} ",
                    platformId, branch);

        List<StatsInternalDTO> stats;
        try {
            stats = repositoryService.getStats(getUserId(authentication), platformId, branch, mappedByAssignments);
        } catch (NoProviderFoundException e) {
            throw new InternalServerErrorException();
        } catch (GitException e) {
            throw new BadRequestException();
        }
        return statsMapper.dtosToDTOs(stats);
    }

    @DeleteMapping("/{platformId}/assignment/{subAssignmentId}")
    @SecurityAnnotations.UserOwnsRepo
    @Operation(description = "Get assignment for committers for a repository", responses = {
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "400", content = @Content(
            mediaType = "application/json",
            schema = @Schema(hidden = true)))
    })
    public void deleteAssignment(final Authentication authentication,
                                 final @PathVariable("platformId") Long platformId,
                                 final @PathVariable("subAssignmentId") Long subAssignmentId) {
        LOGGER.info("DELETE /repository/{id}/assignment/{subAssignmentId} - delete assignment from repository "
                        + "by platform id {} and subAssignmentId {}",
                    platformId, subAssignmentId);

        repositoryService.deleteAssignment(getUserId(authentication), platformId, subAssignmentId);
    }
}

