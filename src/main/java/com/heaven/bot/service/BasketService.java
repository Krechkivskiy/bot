package com.heaven.bot.service;

import com.heaven.bot.model.Basket;
import com.heaven.bot.model.Product;
import com.heaven.bot.model.User;

import java.util.Optional;


public interface BasketService {

    Basket addProduct(Product product, User user);

    void setComplete(Long basketId);

    Basket getBasketByUser(User user);

    Basket findById(Long id);

    Basket save(Basket basketByUser);

    Basket remove(Long productId, String userId);

    Basket setCount(String userId, Long valueOf);

    void createNewBasketForUser(User user);

}
