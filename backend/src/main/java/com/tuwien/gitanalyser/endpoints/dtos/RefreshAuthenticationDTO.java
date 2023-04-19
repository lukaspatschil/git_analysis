package com.tuwien.gitanalyser.endpoints.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RefreshAuthenticationDTO {

    private String accessToken;
    private String refreshToken;
}
