package com.tuwien.gitanalyser.endpoints.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class RefreshAuthenticationInternalDTO {

    private String accessToken;
    private String refreshToken;
    private String fingerprint;
}
