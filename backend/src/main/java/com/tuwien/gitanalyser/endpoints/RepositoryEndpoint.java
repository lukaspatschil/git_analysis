package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.dtos.BranchDTO;
import com.tuwien.gitanalyser.endpoints.dtos.CommitDTO;
import com.tuwien.gitanalyser.endpoints.dtos.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.entity.mapper.BranchMapper;
import com.tuwien.gitanalyser.entity.mapper.CommitMapper;
import com.tuwien.gitanalyser.entity.mapper.NotSavedRepositoryMapper;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/apiV1/repository")
public class RepositoryEndpoint extends BaseEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryEndpoint.class);
    private final RepositoryService repositoryService;
    private final NotSavedRepositoryMapper notSavedRepositoryMapper;
    private final BranchMapper branchMapper;
    private final CommitMapper commitsMapper;

    public RepositoryEndpoint(final RepositoryService repositoryService,
                              final NotSavedRepositoryMapper notSavedRepositoryMapper,
                              final BranchMapper branchMapper,
                              final CommitMapper commitMapper) {
        this.repositoryService = repositoryService;
        this.notSavedRepositoryMapper = notSavedRepositoryMapper;
        this.branchMapper = branchMapper;
        this.commitsMapper = commitMapper;
    }

    @GetMapping
    public List<NotSavedRepositoryDTO> getAllRepositories(final Authentication authentication) {
        LOGGER.info("GET /repository - get all repositories");
        return notSavedRepositoryMapper.dtosToDTOs(
            repositoryService.getAllRepositories(getUserId(authentication))
        );
    }

    @GetMapping("/{platformId}")
    public NotSavedRepositoryDTO getRepositoryById(
        final Authentication authentication,
        final @PathVariable Long platformId) {
        LOGGER.info("GET /repository/{id} - get repository by platform id {}", platformId);
        try {
            return notSavedRepositoryMapper.dtoToDTO(
                repositoryService.getRepositoryById(getUserId(authentication), platformId)
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
            return branchMapper.dtosToDTOs(repositoryService.getAllBranches(getUserId(authentication), platformId));
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
            return commitsMapper.dtosToDTOs(repositoryService.getAllCommits(getUserId(authentication),
                                                                            platformId, branch));
        } catch (GitLabApiException | IOException e) {
            LOGGER.error("Error while getting commits", e);
            throw new RuntimeException(e);
        }
    }
}
