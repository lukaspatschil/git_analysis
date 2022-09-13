package com.tuwien.gitanalyser.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public class LoginEndpoint {

    @GetMapping("/login")
    public String getLogin() {
        return "getLogin";
    }

    @PostMapping("/login")
    public String postLogin() {
        return "postLogin";
    }

}
