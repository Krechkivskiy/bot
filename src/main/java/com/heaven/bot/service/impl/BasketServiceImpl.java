package com.heaven.bot.service.impl;

import com.heaven.bot.model.Basket;
import com.heaven.bot.model.Product;
import com.heaven.bot.model.User;
import com.heaven.bot.repository.BasketRepository;
import com.heaven.bot.repository.ProductRepository;
import com.heaven.bot.repository.UserRepository;
import com.heaven.bot.service.BasketService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BasketServiceImpl implements BasketService {

    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public BasketServiceImpl(BasketRepository basketRepository,
                             ProductRepository productRepository, UserRepository userRepository) {
        this.basketRepository = basketRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    @Override
    public Basket addProduct(Product prod, User user) {
        Basket basketByUser = getBasketByUser(user);
        Product productFromDb = productRepository
                .findById(prod.getId()).orElseThrow(NoSuchElementException::new);
        ifOtherDayProductIsPresentRemove(basketByUser, productFromDb.getDay());
        if (basketByUser.getProducts().contains(productFromDb)) {
            return basketByUser;
        }
        basketByUser.getProducts().add(productFromDb);
        return basketRepository.save(basketByUser);
    }

    private void ifOtherDayProductIsPresentRemove(Basket basketByUser, String day) {
        List<Product> productBasket = basketByUser.getProducts();
        productBasket
                .stream()
                .filter(pb -> !pb.getDay().equals(day))
                .collect(Collectors.toList())
                .forEach(productBasket::remove);
        basketRepository.save(basketByUser);
    }

    @Override
    public void setComplete(Long basketId) {
        Basket basket = basketRepository
                .findById(basketId).orElseThrow(NoSuchElementException::new);
        basket.setFinished(true);
        basketRepository.save(basket);
    }

    @Override
    public Basket getBasketByUser(User user) {
        Basket byUser = basketRepository.findByUser(user.getId());
        if (byUser == null) {
            return createNewBasket(user);
        }
        return byUser;
    }

    private Basket createNewBasket(User user) {
        Basket def = new Basket();
        def.setFinished(false);
        Basket basket = basketRepository.save(def);
        basket.setUser(userRepository.findById(user.getId()).orElseThrow(NoSuchElementException::new));
        return basketRepository.save(basket);
    }

    @Override
    public Basket findById(Long id) {
        return basketRepository
                .findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public Basket save(Basket basketByUser) {
        return basketRepository.save(basketByUser);
    }

    @Override
    public Basket remove(Long productId, String userId) {
        Basket byUser = basketRepository.findByUser(userId);
        Product product1 = byUser.getProducts().stream()
                .filter(product -> product.getId().equals(productId.toString()))
                .findFirst().orElseThrow(NoSuchElementException::new);
        byUser.getProducts().remove(product1);
        return basketRepository.save(byUser);
    }

    @Override
    public Basket setCount(String userId, Long newCount) {
        Basket byUser = basketRepository.findByUser(userId);
        byUser.setCount(newCount);
        return basketRepository.save(byUser);
    }

    @Override
    public void createNewBasketForUser(User user) {
        createNewBasket(user);
    }
}
