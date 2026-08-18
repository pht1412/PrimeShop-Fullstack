// package com.primeshop.stock;

// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// import java.time.LocalDateTime;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class BusinessResponse {
//     private Long id;
//     private String name;
//     private String description;
//     private Boolean active;
//     private LocalDateTime createdAt;
//     private LocalDateTime updatedAt;
    
//     public BusinessResponse(Business business) {
//         this.id = business.getId();
//         this.name = business.getName();
//         this.description = business.getDescription();
//         this.active = business.getActive();
//         this.createdAt = business.getCreatedAt();
//         this.updatedAt = business.getUpdatedAt();
//     }
// } 