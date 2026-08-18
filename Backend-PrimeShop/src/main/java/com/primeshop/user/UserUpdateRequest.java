package com.primeshop.user;

import lombok.Data;

@Data
public class UserUpdateRequest {
   private String fullName;
   private String phoneNumber;
   private String address;
   private String avatar;
}
