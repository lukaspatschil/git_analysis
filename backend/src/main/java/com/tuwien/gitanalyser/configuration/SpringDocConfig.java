package com.tuwien.gitanalyser.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@SecurityScheme(name = "oauth2",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER,
    paramName = HttpHeaders.AUTHORIZATION,
    scheme = "Bearer"
)
public class SpringDocConfig {
}
