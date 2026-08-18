package com.primeshop.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.primeshop.seller.SellerProfile;
import com.primeshop.seller.SellerRepo;


@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SellerRepo sellerRepo;

    public UserResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepo.findByUsername(username);
        User user = optionalUser.get();
        UserResponse userResponse = new UserResponse(user);
        return userResponse;
    }

    public User updateUser(UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepo.findByUsername(username);
        User user = optionalUser.get();        
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        userRepo.save(user);
        return user;
    }

    public Long countUser() {
        return userRepo.countByUsername();
    }

    public void logOut() {
        SecurityContextHolder.clearContext();
    }

    public List<UserResponse> getAllUser() {
        List<User> users = userRepo.findAll();
        List<UserResponse> userResponses = new ArrayList<>();
        for (User user : users) {
            userResponses.add(new UserResponse(user));
        }
        return userResponses;
    }

    public boolean isSeller(Long userId) {
        Optional<SellerProfile> seller = sellerRepo.findByUserId(userId);
        return (seller.get() == null) ? false : true;
    }
}