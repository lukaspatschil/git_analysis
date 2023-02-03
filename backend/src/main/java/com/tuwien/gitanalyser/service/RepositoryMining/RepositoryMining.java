package com.tuwien.gitanalyser.service.RepositoryMining;

import java.io.IOException;

public interface RepositoryMining {
    void getCommits(String accessToken,
                    String clientName,
                    String repositoryUrl,
                    String branch) throws IOException;
}
