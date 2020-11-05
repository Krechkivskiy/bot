package com.heaven.bot.service.impl;

import com.heaven.bot.model.Product;
import com.heaven.bot.repository.ProductRepository;
import com.heaven.bot.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product findById(String productId) {
        return productRepository.findById(productId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<Product> findProductsByDayAndType(String day, String type) {
        return productRepository.findAllByDayAndRole(day, type);
    }
}
