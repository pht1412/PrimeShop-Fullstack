// package com.primeshop.stock;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

// import com.primeshop.product.Product;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.OneToMany;
// import jakarta.persistence.PrePersist;
// import jakarta.persistence.PreUpdate;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Entity
// @Table(name = "business")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class Business {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(name = "name", nullable = false, length = 255)
//     private String name;

//     @Column(name = "description", columnDefinition = "text")
//     private String description;

//     @Column(name = "active", nullable = false)
//     private Boolean active = true;

//     @Column(name = "created_at", updatable = false)
//     private LocalDateTime createdAt;

//     @Column(name = "updated_at")
//     private LocalDateTime updatedAt;

//     @OneToMany(mappedBy = "business")
//     private List<Product> products = new ArrayList<>();

//     @PrePersist
//     public void onCreate() {
//         this.createdAt = LocalDateTime.now();
//         this.updatedAt = LocalDateTime.now();
//     }
    
//     @PreUpdate
//     public void onUpdate() {
//         this.updatedAt = LocalDateTime.now();
//     }
// } 