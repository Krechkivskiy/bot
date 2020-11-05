package com.heaven.bot.service;

import com.heaven.bot.model.Product;

import java.util.List;

public interface ProductService {

    Product findById(String productId);

    List<Product> findProductsByDayAndType(String day, String type);
}
