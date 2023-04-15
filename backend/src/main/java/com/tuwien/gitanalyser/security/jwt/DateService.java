package com.tuwien.gitanalyser.security.jwt;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DateService {

    public DateService() {
    }

    public Date create() {
        return new Date();
    }
}
