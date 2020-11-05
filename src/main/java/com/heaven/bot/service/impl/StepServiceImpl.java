package com.heaven.bot.service.impl;

import com.heaven.bot.model.Step;
import com.heaven.bot.model.User;
import com.heaven.bot.repository.StepRepository;
import com.heaven.bot.repository.UserRepository;
import com.heaven.bot.service.StepService;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class StepServiceImpl implements StepService {

    private final StepRepository stepRepository;
    private final UserRepository userRepository;

    public StepServiceImpl(StepRepository stepRepository, UserRepository userRepository) {
        this.stepRepository = stepRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Step save(Step step, User user) {
        Step save = stepRepository.save(step);
        User fromDb = userRepository.findById(user.getId()).orElseThrow(NoSuchElementException::new);
        save.setUser(fromDb);
        return stepRepository.save(save);
    }

    @Override
    public Optional<Step> findLastUserAction(User user) {
        return Optional.ofNullable(stepRepository
                .findByUser(user.getId()));
    }

    @Override
    public Optional<Step> findLastUserActionByType(User user, String type) {
        return stepRepository.findLastUserActionByType(user.getId(), type);
    }

    @Override
    public void removeAll() {
        stepRepository.deleteAll();
    }
}
