package com.tuwien.gitanalyser.service;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.util.List;

public interface RepositoryService {
    List<RepositoryDTO> getAllRepositories(OAuth2AuthorizedClient client);

}
