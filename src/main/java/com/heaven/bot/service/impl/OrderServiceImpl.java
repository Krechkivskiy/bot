package com.heaven.bot.service.impl;

import com.heaven.bot.model.Order;
import com.heaven.bot.repository.OrderRepository;
import com.heaven.bot.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }
}
