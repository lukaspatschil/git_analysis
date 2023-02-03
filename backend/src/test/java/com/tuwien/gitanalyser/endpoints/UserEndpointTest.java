package com.tuwien.gitanalyser.endpoints;

import com.tuwien.gitanalyser.endpoints.DTOs.UserDTO;
import com.tuwien.gitanalyser.entity.User;
import com.tuwien.gitanalyser.entity.mapper.UserMapper;
import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import com.tuwien.gitanalyser.exception.NotFoundException;
import com.tuwien.gitanalyser.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.core.Authentication;
import utils.Randoms;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserEndpointTest {

    UserEndpoint sut;
    private UserMapper userMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userMapper = mock(UserMapper.class);
        sut = new UserEndpoint(userService, userMapper);
    }

    @Test
    void getUser_always_shouldCallService() throws NotFoundException {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(String.valueOf(Randoms.getLong()));

        // When
        sut.getLoggedInUser(authentication);

        // Then
        verify(userService).getUser(Long.parseLong(authentication.getName()));
    }

    @ParameterizedTest
    @EnumSource(AuthenticationProvider.class)
    void getUser_serviceReturnsUser_callsMapper(AuthenticationProvider authenticationProvider) throws NotFoundException {
        // Given
        Authentication authentication = mock(Authentication.class);
        User user = new User(Randoms.getLong(), Randoms.alpha(), Randoms.alpha(), Randoms.alpha(),
                             authenticationProvider, Randoms.integer(), Randoms.alpha(), Randoms.alpha());
        long userId = Randoms.getLong();

        when(authentication.getName()).thenReturn(String.valueOf(userId));
        when(userService.getUser(userId)).thenReturn(user);

        // When
        sut.getLoggedInUser(authentication);

        // Then
        verify(userMapper).entityToDTO(user);
    }

    @ParameterizedTest
    @EnumSource(AuthenticationProvider.class)
    void getUser_serviceReturnsUser_returnsValueFromMapper(AuthenticationProvider authenticationProvider) throws NotFoundException {
        // Given
        Authentication authentication = mock(Authentication.class);
        long userId = Randoms.getLong();
        User user = new User(Randoms.getLong(), Randoms.alpha(), Randoms.alpha(), Randoms.alpha(),
                             authenticationProvider, Randoms.integer(), Randoms.alpha(), Randoms.alpha());
        UserDTO userDTO = new UserDTO(user.getId(), user.getUsername(),user.getEmail(), user.getPictureUrl());


        when(authentication.getName()).thenReturn(String.valueOf(userId));
        when(userService.getUser(userId)).thenReturn(user);
        when(userMapper.entityToDTO(user)).thenReturn(userDTO);

        // When
        UserDTO result = sut.getLoggedInUser(authentication);

        // Then
        assertThat(result, equalTo(userDTO));
    }

}