package com.tuwien.gitanalyser.endpoints;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController()
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelloEndpoint {

    @GetMapping()
    public Map<String, Object> index(final @RequestParam(required = false, value = "code") String code) {
        if (code != null) {
            return Collections.singletonMap("code", code);
        }
        return Collections.emptyMap();
    }

}
