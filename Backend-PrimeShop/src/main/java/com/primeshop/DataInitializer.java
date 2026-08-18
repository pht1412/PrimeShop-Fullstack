package com.primeshop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// import com.primeshop.stock.BusinessService;
import com.primeshop.user.Role;
import com.primeshop.user.RoleRepo;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initRoles(RoleRepo roleRepository) {
        return args -> {
            if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(Role.RoleName.ROLE_ADMIN));
            }
            if (roleRepository.findByName(Role.RoleName.ROLE_BUSSINESS).isEmpty()) {
                roleRepository.save(new Role(Role.RoleName.ROLE_BUSSINESS));
            }
            if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(Role.RoleName.ROLE_USER));
            }
            
            // Khởi tạo dữ liệu mặc định cho Business
            // businessService.initializeDefaultBusinesses();
        };
    }
}
