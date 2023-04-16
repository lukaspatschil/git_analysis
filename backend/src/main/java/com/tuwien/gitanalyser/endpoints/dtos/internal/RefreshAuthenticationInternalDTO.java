package com.tuwien.gitanalyser.endpoints.dtos.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshAuthenticationInternalDTO {

    private String accessToken;
    private String refreshToken;
    private String fingerprint;
}
