package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.BranchDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.NotSavedRepositoryDTO;
import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.entity.mapper.BranchMapper;
import com.tuwien.gitanalyser.entity.mapper.NotSavedRepositoryMapper;
import com.tuwien.gitanalyser.entity.mapper.RepositoryMapper;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/apiV1/repository")
public class RepositoryEndpoint extends BaseEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryEndpoint.class);

    private final RepositoryService repositoryService;

    private final RepositoryMapper repositoryMapper;

    private final NotSavedRepositoryMapper notSavedRepositoryMapper;

    private final BranchMapper branchMapper;

    public RepositoryEndpoint(final RepositoryService repositoryService,
                              final RepositoryMapper repositoryMapper,
                              final NotSavedRepositoryMapper notSavedRepositoryMapper,
                              final BranchMapper branchMapper) {
        this.repositoryService = repositoryService;
        this.repositoryMapper = repositoryMapper;
        this.notSavedRepositoryMapper = notSavedRepositoryMapper;
        this.branchMapper = branchMapper;
    }

    @GetMapping
    public List<NotSavedRepositoryDTO> getAllRepositories(final Authentication authentication) {
        LOGGER.info("GET /repository - get all repositories");
        return notSavedRepositoryMapper.dtosToDTOs(
            repositoryService.getAllRepositories(getUserId(authentication))
        );
    }

    @GetMapping("/{id}")
    public RepositoryDTO getRepositoryById(
        final Authentication authentication,
        final @PathVariable Long id) {
        LOGGER.info("GET /repository/{id} - get repository by platform id {}", id);
        return repositoryMapper.entityToDTO(
            repositoryService.getRepositoryById(getUserId(authentication), id)
        );
    }

    @GetMapping("/{id}/branch")
    public List<BranchDTO> getBranchesByRepositoryId(
        final Authentication authentication,
        final @PathVariable Long id) {
        LOGGER.info("GET /repository/{id}/branch - get repository by platform id {}", id);
        return branchMapper.dtosToDTOs(repositoryService.getAllBranches(getUserId(authentication), id));
    }
}
