package com.tuwien.gitanalyser.endpoints.DTOs;

import com.tuwien.gitanalyser.entity.utils.AuthenticationProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String username;

    private String email;

    private AuthenticationProvider authenticationProvider;

    private Integer platformId;
}
