package com.heaven.bot.service.impl;

import com.heaven.bot.model.User;
import com.heaven.bot.repository.StepRepository;
import com.heaven.bot.repository.UserRepository;
import com.heaven.bot.service.UserService;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }
}
