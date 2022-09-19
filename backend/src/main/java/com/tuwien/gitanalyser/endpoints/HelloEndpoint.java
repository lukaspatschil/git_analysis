package com.tuwien.gitanalyser.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloEndpoint {

    /**
     * get hello.
     * @return String hallo
     */
    @GetMapping("/")
    public String index() {
        return "Hallo";
    }

}
