package com.heaven.bot.repository;

import com.heaven.bot.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<Product, String> {

    List<Product> findAll();

    List<Product> findAllByDayAndRole(String day, String role);
}
