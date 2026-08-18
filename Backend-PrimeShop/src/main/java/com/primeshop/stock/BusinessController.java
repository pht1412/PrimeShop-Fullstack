// package com.primeshop.stock;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import jakarta.validation.Valid;
// import java.util.List;

// @RestController
// @RequestMapping("/api/business")
// @CrossOrigin(origins = "http://localhost:5173")
// public class BusinessController {
    
//     @Autowired
//     private BusinessService businessService;
    
//     @GetMapping
//     public ResponseEntity<List<BusinessResponse>> getAllBusinesses() {
//         List<BusinessResponse> businesses = businessService.getAllBusinesses();
//         return ResponseEntity.ok(businesses);
//     }
    
//     @GetMapping("/{id}")
//     public ResponseEntity<BusinessResponse> getBusinessById(@PathVariable Long id) {
//         return businessService.getBusinessById(id)
//             .map(ResponseEntity::ok)
//             .orElse(ResponseEntity.notFound().build());
//     }
    
//     @PostMapping
//     public ResponseEntity<BusinessResponse> createBusiness(@Valid @RequestBody BusinessRequest request) {
//         try {
//             BusinessResponse business = businessService.createBusiness(request);
//             return ResponseEntity.ok(business);
//         } catch (RuntimeException e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }
    
//     @PutMapping("/{id}")
//     public ResponseEntity<BusinessResponse> updateBusiness(@PathVariable Long id, @Valid @RequestBody BusinessRequest request) {
//         return businessService.updateBusiness(id, request)
//             .map(ResponseEntity::ok)
//             .orElse(ResponseEntity.notFound().build());
//     }
    
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
//         boolean deleted = businessService.deleteBusiness(id);
//         return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
//     }
    
//     @PostMapping("/initialize")
//     public ResponseEntity<String> initializeDefaultBusinesses() {
//         businessService.initializeDefaultBusinesses();
//         return ResponseEntity.ok("Default businesses initialized successfully");
//     }
// } 