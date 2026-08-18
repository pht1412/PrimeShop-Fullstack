// package com.primeshop.stock;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// @Service
// public class BusinessService {
    
//     @Autowired
//     private BusinessRepo businessRepo;
    
//     public List<BusinessResponse> getAllBusinesses() {
//         return businessRepo.findByActiveTrue()
//             .stream()
//             .map(BusinessResponse::new)
//             .collect(Collectors.toList());
//     }
    
//     public Optional<BusinessResponse> getBusinessById(Long id) {
//         return businessRepo.findByIdAndActiveTrue(id)
//             .map(BusinessResponse::new);
//     }
    
//     public BusinessResponse createBusiness(BusinessRequest request) {
//         if (businessRepo.existsByName(request.getName())) {
//             throw new RuntimeException("Business với tên này đã tồn tại");
//         }
        
//         Business business = new Business();
//         business.setName(request.getName());
//         business.setDescription(request.getDescription());
//         business.setActive(true);
        
//         Business savedBusiness = businessRepo.save(business);
//         return new BusinessResponse(savedBusiness);
//     }
    
//     public Optional<BusinessResponse> updateBusiness(Long id, BusinessRequest request) {
//         return businessRepo.findByIdAndActiveTrue(id)
//             .map(business -> {
//                 if (!business.getName().equals(request.getName()) && 
//                     businessRepo.existsByName(request.getName())) {
//                     throw new RuntimeException("Business với tên này đã tồn tại");
//                 }
                
//                 business.setName(request.getName());
//                 business.setDescription(request.getDescription());
                
//                 Business savedBusiness = businessRepo.save(business);
//                 return new BusinessResponse(savedBusiness);
//             });
//     }
    
//     public boolean deleteBusiness(Long id) {
//         return businessRepo.findByIdAndActiveTrue(id)
//             .map(business -> {
//                 business.setActive(false);
//                 businessRepo.save(business);
//                 return true;
//             })
//             .orElse(false);
//     }
    
//     public void initializeDefaultBusinesses() {
//         // Kiểm tra xem đã có business nào chưa
//         if (businessRepo.count() == 0) {
//             // Tạo business cho hàng tự sản xuất (ID = 1)
//             Business selfProduced = new Business();
//             selfProduced.setName("Hàng tự sản xuất");
//             selfProduced.setDescription("Các sản phẩm do công ty tự sản xuất");
//             selfProduced.setActive(true);
//             businessRepo.save(selfProduced);
            
//             // Tạo business cho hàng từ nguồn khác (ID = 2)
//             Business externalSource = new Business();
//             externalSource.setName("Hàng từ nguồn khác");
//             externalSource.setDescription("Các sản phẩm nhập từ nhà cung cấp bên ngoài");
//             externalSource.setActive(true);
//             businessRepo.save(externalSource);
//         }
//     }
// } 