package com.tuwien.gitanalyser.security.jwt;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface JWTTokenProvider {

    /**
     * creates a JWT token for the given user id.
     *
     * @param id of the user in the database
     * @return JWT token string
     */
    String createToken(Long id);

    /**
     * creates an authentication if the jwt is valid and the user is in the database.
     *
     * @param token JWT token string
     * @return user id
     */
    Authentication getAuthentication(String token);

    /**
     * extracts the user id from the JWT token.
     *
     * @param token JWT token
     * @return user id
     */
    Long getUserId(String token);

    /**
     * extracts the token from the request header and removes the Bearer Prefix.
     *
     * @param request http request
     * @return token string
     */
    String resolveToken(HttpServletRequest request);

    /**
     * validates the token.
     *
     * @param token JWT token
     * @return true if token is valid
     */
    boolean validateToken(String token);
}
