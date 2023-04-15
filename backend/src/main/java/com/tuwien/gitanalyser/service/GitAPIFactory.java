package com.tuwien.gitanalyser.service;

import java.io.IOException;

public interface GitAPIFactory<T> {

    T createObject(String accessToken) throws IOException;
}
