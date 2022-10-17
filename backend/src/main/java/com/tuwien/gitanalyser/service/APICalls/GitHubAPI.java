package com.tuwien.gitanalyser.service.APICalls;

import com.tuwien.gitanalyser.endpoints.DTOs.RepositoryDTO;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubAPI implements GitAPI {

    public List<RepositoryDTO> getAllRepositories(String accessToken) throws IOException {
        List<RepositoryDTO> allRepos = new ArrayList<>();

        GitHub github = GitHub.connectUsingOAuth(accessToken);
        github.getMyself().getAllRepositories().values()
              .forEach(repo -> allRepos.add(new RepositoryDTO(repo.getId(), repo.getName())));

        return allRepos;
    }
}
