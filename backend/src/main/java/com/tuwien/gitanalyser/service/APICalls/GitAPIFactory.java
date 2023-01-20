package com.tuwien.gitanalyser.service.APICalls;

import java.io.IOException;

public interface GitAPIFactory {

    Object createObject(String accessToken) throws IOException;
}
