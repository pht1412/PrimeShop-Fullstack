// package com.primeshop.stock;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface BusinessRepo extends JpaRepository<Business, Long> {
    
//     List<Business> findByActiveTrue();
    
//     Optional<Business> findByIdAndActiveTrue(Long id);
    
//     boolean existsByName(String name);
// } 