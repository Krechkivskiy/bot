package com.heaven.bot.repository;

import com.heaven.bot.model.Step;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StepRepository extends CrudRepository<Step, Long> {

    @Query(value = "select * from  steps " +
            " where user_id =:userId and step !='back'"
            + "order by id desc limit 1;"
            , nativeQuery = true)
    Step findByUser(String userId);

    @Query(value = "select * from  steps " +
            " where user_id =:userId and step =:type "
            + "order by id desc limit 1;"
            , nativeQuery = true)
    Optional<Step> findLastUserActionByType(String userId, String type);
}
