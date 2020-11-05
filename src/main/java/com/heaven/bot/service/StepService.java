package com.heaven.bot.service;

import com.heaven.bot.model.Step;
import com.heaven.bot.model.User;

import java.util.Optional;

public interface StepService {

    Step save(Step step, User user);

    Optional<Step> findLastUserAction(User user);

    Optional<Step> findLastUserActionByType(User user, String string);

    void removeAll();

}
