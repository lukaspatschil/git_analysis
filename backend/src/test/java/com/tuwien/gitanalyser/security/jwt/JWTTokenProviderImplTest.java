package com.tuwien.gitanalyser.security.jwt;

import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.exception.AuthenticationException;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.security.AuthenticationConstants;
import com.tuwien.gitanalyser.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import utils.Randoms;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JWTTokenProviderImplTest {

    private static final List<GrantedAuthority> GRANTED_AUTHORITIES =
        AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
    JWTTokenProviderImpl sut;
    private DateService dateService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        dateService = mock(DateService.class);
        sut = new JWTTokenProviderImpl(userService, dateService);
    }

    @Test
    void createToken_always_shouldReturnString() {
        // When
        long randomTime = Randoms.getLong();
        Date date = new Date();
        date.setTime(randomTime);
        long userId = 1L;

        when(dateService.create()).thenReturn(date);

        // Given
        String result = sut.createToken(userId);

        // Then
        assertNotNull(result);
    }

    @Test
    void getAuthentication_userAvailable_shouldReturnAuthentication() throws NotFoundException {
        // Given
        long userId = Randoms.getLong();

        User user = new User();
        user.setId(userId);

        String accessToken = createToken(userId, new Date());

        when(userService.getUser(userId)).thenReturn(user);

        // When
        Authentication result = sut.getAuthentication(accessToken);

        // Then
        assertThat(result.getName(), equalTo(user.getId().toString()));
    }

    @Test
    void getAuthentication_userNotAvailableAndThrowsNotFoundException_shouldThrowAuthenticationException() throws NotFoundException {
        // Given
        long userId = Randoms.getLong();

        User user = new User();
        user.setId(userId);

        String accessToken = createToken(userId, new Date());

        when(userService.getUser(userId)).thenThrow(new NotFoundException());

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.getAuthentication(accessToken));
    }

    @Test
    void getAuthentication_noValidToken_shouldThrowAuthenticationException() {
        // Given
        long userId = Randoms.getLong();
        String accessToken = Randoms.alpha();

        User user = new User();
        user.setId(userId);

        // When + Then
        assertThrows(AuthenticationException.class, () -> sut.getAuthentication(accessToken));
    }

    public String createToken(final Long id, final Date now) {

        Claims claims = Jwts.claims().setSubject(id.toString());

        Date validity = new Date(now.getTime() + AuthenticationConstants.JWT_VALIDITY_IN_MILLISECONDS);

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
}