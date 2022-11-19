package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import com.tuwien.gitanalyser.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
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

    public RepositoryEndpoint(final RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping
    public List<RepositoryDTO> getAllRepositories(
        final @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) {
        LOGGER.info("GET /repository -  get all repositories");
        return repositoryService.getAllRepositories(client);
    }

    @GetMapping("/{id}")
    public RepositoryDTO getRepositoryById(
        final @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client,
        final @PathVariable Long id) {
        LOGGER.info("GET /repository/{id} -  get repository by id");
        return repositoryService.getRepositoryById(client, id);
    }
}
