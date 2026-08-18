package com.primeshop.user;

import lombok.Data;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")

public class AuthController {
    private final AuthService authService;
    private final UserRepo userRepo;

    public AuthController(AuthService authService, UserRepo userRepository) {
        this.authService = authService;
        this.userRepo = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole(),
                request.getFullName(),
                request.getPhoneNumber(),
                request.getAddress(),
                request.getAvatar()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getUsername(), request.getPassword());
            User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            Map<String, Object> userData = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
                //
            );

            return ResponseEntity.ok(Map.of(
                "token", token,
                "user", userData
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check-role")
    public ResponseEntity<?> checkRole() {
        List<String> roles = authService.getCurrentUserRoles();
        return ResponseEntity.ok(roles);
    }
}

@Data
class ErrorResponse {
    private String error;
    public ErrorResponse(String error) {
        this.error = error;
    }
}