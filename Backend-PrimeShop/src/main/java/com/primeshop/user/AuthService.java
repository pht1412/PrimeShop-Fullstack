package com.primeshop.user;

import com.primeshop.security.JwtUtil;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {   
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User register(String username, String password, String email, String role, String fullName, String phoneNumber, String address, String avatar) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        Role.RoleName roleEnum;
        try {
            roleEnum = Role.RoleName.valueOf("ROLE_" + role.toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new RuntimeException("Vai trò không hợp lệ: " + role);
        }

        Role userRole = roleRepository.findByName(roleEnum)
            .orElseThrow(() -> new RuntimeException("Role không tồn tại trong hệ thống"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.getRoles().add(userRole);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setAvatar(avatar);
        return userRepo.save(user);
    }

    public String login(String username, String password) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Sai mật khẩu!");
        }

        List<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toList());

        System.out.println("Đăng nhập thành công cho user: " + username);        
        return jwtUtil.generateToken(user.getUsername(), roles);
    }

    public List<String> getCurrentUserRoles() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found!"));

        return user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toList());
    }
}