package com.primeshop.seller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;
import com.primeshop.product.ProductResponse;
import com.primeshop.product.Product.ProductStatus;
import com.primeshop.seller.SellerProfile.SellerStatus;
import com.primeshop.user.Role;
import com.primeshop.user.RoleRepo;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;

@Service
public class SellerService {
    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final SellerRepo sellerRepo;
    @Autowired
    private final ProductRepo productRepo;
    @Autowired
    private final RoleRepo roleRepo;

    public SellerService(UserRepo userRepo, SellerRepo sellerRepo, ProductRepo productRepo, RoleRepo roleRepo) {
        this.userRepo = userRepo;
        this.sellerRepo = sellerRepo;
        this.productRepo = productRepo;
        this.roleRepo = roleRepo;
    }

    public SellerResponse registerSeller(SellerRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        System.out.println("============================" + user.getId());
        if (!sellerRepo.findByUserId(user.getId()).isEmpty()) {
            throw new IllegalArgumentException("Seller profile already exists for this user");
        }

        Role sellerRole = roleRepo.findByName(Role.RoleName.ROLE_BUSSINESS)
            .orElseThrow(() -> new RuntimeException("Role không tồn tại trong hệ thống"));


        SellerProfile sellerProfile = new SellerProfile();
        sellerProfile.setUser(user);
        sellerProfile.setShopName(request.getShopName());
        sellerProfile.setIdentityCard(request.getIdentityCard());
        sellerProfile.setDescription(request.getDescription());
        sellerProfile.setPhone(request.getPhone());
        sellerProfile.setStatus(SellerStatus.PENDING_REVIEW);
        sellerRepo.save(sellerProfile);
        user.getRoles().add(sellerRole);
        userRepo.save(user);
        return new SellerResponse(sellerProfile);
    }

    public SellerProfile approveSeller(Long sellerId) {
        SellerProfile sellerProfile = sellerRepo.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller profile not found"));
        if (sellerProfile.getStatus() != SellerStatus.PENDING_REVIEW) {
            throw new IllegalArgumentException("Seller profile is not in pending review status");
        }
        sellerProfile.setStatus(SellerStatus.VERIFIED_SELLER);
        return sellerRepo.save(sellerProfile);
    }

    public ProductResponse approveSellerProducts(Long sellerId, Long productId) {
        SellerProfile sellerProfile = sellerRepo.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller profile not found"));
        if (sellerProfile.getStatus() != SellerStatus.VERIFIED_SELLER) {
            throw new IllegalArgumentException("Seller profile is not verified");
        }
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        System.out.println("============================" + product.getStatus());
        if (product.getStatus() != ProductStatus.PENDING) {
            throw new IllegalArgumentException("Product is not in pending status");
        }
        product.setStatus(ProductStatus.APPROVED);
        productRepo.save(product);
        return new ProductResponse(product);
    }

    public SellerResponse getSellerProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        return new SellerResponse(sellerRepo.findByUserId(user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Seller profile not found")));
    }
    // [ADD] Hàm nghiệp vụ: Lấy danh sách người bán đang chờ duyệt (PENDING_REVIEW)
    public List<SellerResponse> getPendingSellers() {
        List<SellerProfile> pendingSellers = sellerRepo.findByStatus(SellerStatus.PENDING_REVIEW);
        return pendingSellers.stream()
                .map(SellerResponse::new)
                .collect(Collectors.toList());
    }

    // [ADD] Hàm nghiệp vụ: Lấy danh sách TẤT CẢ sản phẩm đang chờ duyệt (PENDING)
    // Admin cần xem toàn bộ sản phẩm mới đăng của sàn để duyệt
    public List<ProductResponse> getPendingProducts() {
        List<Product> pendingProducts = productRepo.findByStatus(ProductStatus.PENDING);
        return pendingProducts.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    public SellerResponse banSeller(Long sellerId) {
        SellerProfile sellerProfile = sellerRepo.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller profile not found"));
        
        // Cập nhật trạng thái sang BANNED_SELLER
        sellerProfile.setStatus(SellerStatus.BANNED_SELLER);
        
        // Lưu thay đổi xuống Database
        sellerRepo.save(sellerProfile);
        
        return new SellerResponse(sellerProfile);
    }

    // [ADD] Hàm nghiệp vụ: Từ chối duyệt sản phẩm (REJECTED)
    // Dùng khi sản phẩm vi phạm chính sách, ảnh mờ, thông tin sai lệch...
    public ProductResponse rejectProduct(Long sellerId, Long productId) {
        // Kiểm tra Seller có tồn tại
        SellerProfile sellerProfile = sellerRepo.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller profile not found"));
        
        // Kiểm tra Product có tồn tại
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Chỉ từ chối được sản phẩm đang chờ duyệt
        if (product.getStatus() != ProductStatus.PENDING) {
             throw new IllegalArgumentException("Product is not in pending status");
        }

        // Cập nhật trạng thái
        product.setStatus(ProductStatus.REJECTED);
        productRepo.save(product);
        
        return new ProductResponse(product);
    }

    // [ADD] Hàm nghiệp vụ: Lấy tất cả Seller (Dùng cho Tab "Tất cả doanh nghiệp")
    public List<SellerResponse> getAllSellers() {
        // findAll() là hàm có sẵn của JPA, không cần viết thêm trong Repo
        List<SellerProfile> allSellers = sellerRepo.findAll();
        return allSellers.stream()
                .map(SellerResponse::new)
                .collect(Collectors.toList());
    }

}
