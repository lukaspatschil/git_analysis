package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;

import java.util.List;

public interface GitAPI {
    List<RepositoryDTO> getAllRepositories(String tokenValue) throws Exception;
}
