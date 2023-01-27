package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
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
public class RepositoryEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryEndpoint.class);

    private final RepositoryService repositoryService;

    private final RepositoryMapper repositoryMapper;

    public RepositoryEndpoint(final RepositoryService repositoryService, final RepositoryMapper repositoryMapper) {
        this.repositoryService = repositoryService;
        this.repositoryMapper = repositoryMapper;
    }

    @GetMapping
    public List<RepositoryDTO> getAllRepositories(final Authentication authentication) {
        LOGGER.info("GET /repository -  get all repositories");
        return repositoryMapper.entitiesToDTOs(
            repositoryService.getAllRepositories(Long.parseLong(authentication.getName()))
        );
    }

    @GetMapping("/{id}")
    public RepositoryDTO getRepositoryById(
        final Authentication authentication,
        final @PathVariable Long id) {
        LOGGER.info("GET /repository/{id} -  get repository by id");
        return repositoryMapper.entityToDTO(
            repositoryService.getRepositoryById(Long.parseLong(authentication.getName()), id)
        );
    }
}
