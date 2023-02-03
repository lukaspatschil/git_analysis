package com.tuwien.gitanalyser.service.RepositoryMining;

import com.tuwien.gitanalyser.security.AuthenticationConstants;
import de.unibremen.informatik.st.libvcs4j.ITEngine;
import de.unibremen.informatik.st.libvcs4j.ITEngineBuilder;
import de.unibremen.informatik.st.libvcs4j.VCSEngine;
import de.unibremen.informatik.st.libvcs4j.VCSEngineBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RepositoryMiningImpl implements RepositoryMining {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryMiningImpl.class);

    @Override
    public void getCommits(final String accessToken,
                           final String clientName,
                           final String repositoryUrl,
                           final String branch) throws IOException {
        VCSEngine engine = createEngine(accessToken, clientName, repositoryUrl, branch);
    }

    private VCSEngine createEngine(final String accessToken,
                                   final String clientName,
                                   final String pathToRepository,
                                   final String branch) throws IOException {
        LOGGER.info("Creating engine for repository: " + pathToRepository + " "
                        + "and branch: " + branch + " clientName: " + clientName);

        ITEngine it = createITEngine(accessToken, clientName, pathToRepository);

        return VCSEngineBuilder
                   .ofGit(pathToRepository)
                   .withITEngine(it)
                   .withBranch(branch).build();
    }

    private ITEngine createITEngine(final String accessToken, final String clientName, final String repositoryUrl)
        throws IOException {

        return switch (clientName) {
            case AuthenticationConstants.GITHUB_CLIENT_NAME ->
                ITEngineBuilder.ofGithub(repositoryUrl).withToken(accessToken).build();
            case AuthenticationConstants.GITLAB_CLIENT_NAME ->
                ITEngineBuilder.of(repositoryUrl).withToken(accessToken).build();
            default -> throw new RuntimeException("No API for this client");
        };
    }
}

