package com.primeshop.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.primeshop.user.User;
import com.primeshop.user.UserRepo;

@Component
public class SecurityUtils {
    @Autowired
    private UserRepo userRepo;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public static boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }

    public static boolean isUser(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER"));
    }
    
    public User getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"))
                .getId();
    }
}
