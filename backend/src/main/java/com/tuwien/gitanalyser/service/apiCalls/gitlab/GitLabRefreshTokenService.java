package com.tuwien.gitanalyser.service.apiCalls.gitlab;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.GitLabException;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.GitRefreshTokenService;
import com.tuwien.gitanalyser.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class GitLabRefreshTokenService implements GitRefreshTokenService {

    private final UserService userService;

    public GitLabRefreshTokenService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public void refreshGitAccessToken(final Long userId) throws GitLabException {
        User user = userService.getUser(userId);

        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder
                      .fromUri(URI.create(AuthenticationConstants.GITLAB_CLIENT_URL + "/oauth/token"))
                      .queryParam("client_id", AuthenticationConstants.GITLAB_CLIENT_ID)
                      .queryParam("refresh_token", user.getRefreshToken())
                      .queryParam("grant_type", "refresh_token")
                      .queryParam("client_secret", AuthenticationConstants.GITLAB_CLIENT_SECRET)
                      .queryParam("redirect_uri", AuthenticationConstants.GITLAB_REDIRECT_URI)
                      .build()
                      .toUri();

        String response = restTemplate.exchange(uri, HttpMethod.POST, null, String.class).getBody();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String accessToken = jsonResponse.getString("refresh_token");
            String refreshToken = jsonResponse.getString("access_token");
            userService.refreshGitAccessToken(userId, accessToken, refreshToken);
        } catch (JSONException e) {
            throw new GitLabException(e);
        }
    }
}
