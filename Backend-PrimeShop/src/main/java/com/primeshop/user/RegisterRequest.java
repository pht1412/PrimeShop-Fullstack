package com.primeshop.user;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String role; 
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatar;
}
