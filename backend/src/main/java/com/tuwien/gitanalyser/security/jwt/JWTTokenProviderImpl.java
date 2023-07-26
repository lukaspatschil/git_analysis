package com.tuwien.gitanalyser.security.jwt;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class JWTTokenProviderImpl implements JWTTokenProvider {

    private static final List<GrantedAuthority> GRANTED_AUTHORITIES = AuthorityUtils
                                                                          .commaSeparatedStringToAuthorityList(
                                                                              "ROLE_USER");
    private final UserService userService;
    private final DateService dateService;
    private final long refreshTokenValidityInDays;
    /**
     * THIS IS NOT A SECURE PRACTICE! For simplicity, we are storing a static key here. Ideally, in a
     * microservices' environment, this key would be kept on a config-server.
     */
    private final long accessTokenValidityInMilliseconds;

    public JWTTokenProviderImpl(final UserService userService, final DateService dateService) {
        this.userService = userService;
        this.dateService = dateService;

        accessTokenValidityInMilliseconds = AuthenticationConstants.JWT_ACCESS_TOKEN_VALIDITY_IN_MILLISECONDS;
        refreshTokenValidityInDays = AuthenticationConstants.JWT_REFRESH_TOKEN_VALIDITY_IN_DAYS;
    }

    @Override
    public String createAccessToken(final Long id) {

        Date now = dateService.create();

        Claims claims = createClaims(id);

        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return createToken(now, claims, validity);
    }

    @Override
    public String createRefreshToken(final Long id) {

        Date now = dateService.create();

        Claims claims = createClaims(id);

        Date validity = new Date(
            now.getTime() + TimeUnit.MILLISECONDS.convert(refreshTokenValidityInDays, TimeUnit.DAYS));

        return createToken(now, claims, validity);
    }

    @Override
    public Authentication getAuthentication(final String token) {
        User user;
        try {
            user = userService.getUser(getUserId(token));
            return new UsernamePasswordAuthenticationToken(user.getId(), "", GRANTED_AUTHORITIES);
        } catch (NotFoundException e) {
            throw new AuthenticationException(e.getMessage());
        }

    }

    @Override
    public Long getUserId(final String token) {
        try {
            return Long.parseLong(
                Jwts.parser()
                    .setSigningKey(AuthenticationConstants.JWT_SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject()
            );
        } catch (JwtException e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public String resolveToken(final HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(AuthenticationConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(AuthenticationConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    @Override
    public boolean validateToken(final String token) {
        try {
            Jwts.parser().setSigningKey(AuthenticationConstants.JWT_SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String createToken(final Date now, final Claims claims, final Date validity) {
        return Jwts.builder()
                   .setClaims(claims)
                   .claim("authorities",
                          GRANTED_AUTHORITIES.stream()
                                             .map(GrantedAuthority::getAuthority)
                                             .collect(Collectors.toList()))
                   .setIssuedAt(now)
                   .setExpiration(validity)
                   .signWith(SignatureAlgorithm.HS256, AuthenticationConstants.JWT_SECRET_KEY)
                   .compact();
    }

    private Claims createClaims(final Long id) {
        return Jwts.claims().setSubject(id.toString());
    }
}
