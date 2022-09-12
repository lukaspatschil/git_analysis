package com.tuwien.gitanalyser.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloEndpoint {

    @GetMapping("/")
    public String index() {
        return "Hallo";
    }

}
