package com.tuwien.gitanalyser;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@SecurityRequirement(name = "oauth2")
@EnableTransactionManagement
@OpenAPIDefinition(info = @Info(title = "GitAnalyser API", version = "v1"))
public class GitAnalyserApplication {

    /**
     * starts the application.
     *
     * @param args arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(GitAnalyserApplication.class, args);
    }

}
