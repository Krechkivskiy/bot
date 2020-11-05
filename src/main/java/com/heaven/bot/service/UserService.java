package com.heaven.bot.service;

import com.heaven.bot.model.User;

public interface UserService {

    User save(User user);

    User findById(String id);
}
