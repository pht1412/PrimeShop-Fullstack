package com.primeshop.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primeshop.user.User;

public interface CartRepo extends JpaRepository<Cart, Long> {
       Optional<Cart> findByUser(User user);
       
}
