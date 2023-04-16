package com.tuwien.gitanalyser.security.jwt;

import com.tuwien.gitanalyser.exception.AuthenticationException;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtTokenFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JWTTokenProvider jwtTokenProviderImpl;

    public JwtTokenFilter(final JWTTokenProviderImpl jwtTokenProviderImpl) {
        Assert.notNull(jwtTokenProviderImpl, "jwtTokenProviderImpl must not be null");
        this.jwtTokenProviderImpl = jwtTokenProviderImpl;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        String token = jwtTokenProviderImpl.resolveToken(request);

        try {
            if (token != null && jwtTokenProviderImpl.validateToken(token)) {
                Authentication auth = jwtTokenProviderImpl.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
            } else {
                handleUnauthorized(response);
            }
        } catch (AuthenticationException ex) {
            handleUnauthorized(response);
        }
    }

    private void handleUnauthorized(final HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
