package com.tuwien.gitanalyser.service.APICalls;

import java.io.IOException;

public interface GitAPIFactory<T> {

    T createObject(String accessToken) throws IOException;
}
