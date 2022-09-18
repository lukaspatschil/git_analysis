package com.tuwien.gitanalyser.service;

import com.sun.istack.NotNull;
import com.tuwien.gitanalyser.entity.User;

import java.util.List;

public interface UserService {
    User getUser(@NotNull Long id);

    List<User> getAll();
}
