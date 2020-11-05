package com.heaven.bot.repository;

import com.heaven.bot.model.Basket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketRepository extends CrudRepository<Basket, Long> {

    @Query(value = "select * from  baskets " +
            " where user_id =:userId and is_finished = false "
            + "order by id desc limit 1;"
            , nativeQuery = true)
    Basket findByUser(String userId);
}
