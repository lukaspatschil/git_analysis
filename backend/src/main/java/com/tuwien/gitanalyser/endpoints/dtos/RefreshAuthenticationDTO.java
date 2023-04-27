package com.tuwien.gitanalyser.endpoints.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshAuthenticationDTO {

    private String accessToken;
    private String refreshToken;
}
