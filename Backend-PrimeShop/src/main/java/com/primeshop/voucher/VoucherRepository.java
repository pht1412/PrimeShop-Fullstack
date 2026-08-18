package com.primeshop.voucher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    
    // Tìm voucher theo mã code
    Optional<Voucher> findByCode(String code);
    
    // Tìm voucher theo mã code và kiểm tra trạng thái active
    Optional<Voucher> findByCodeAndIsActiveTrue(String code);
    
    // Tìm tất cả voucher đang hoạt động
    List<Voucher> findByIsActiveTrue();
    
    // Tìm voucher hợp lệ theo thời gian hiện tại
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND (v.startDate IS NULL OR v.startDate <= :currentTime) " +
           "AND (v.endDate IS NULL OR v.endDate >= :currentTime) " +
           "AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage)")
    List<Voucher> findValidVouchers(@Param("currentTime") LocalDateTime currentTime);
    
    // Tìm voucher hợp lệ cho đơn hàng có giá trị cụ thể
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND (v.startDate IS NULL OR v.startDate <= :currentTime) " +
           "AND (v.endDate IS NULL OR v.endDate >= :currentTime) " +
           "AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage) " +
           "AND (v.minOrderValue IS NULL OR v.minOrderValue <= :orderValue) " +
           "ORDER BY v.discountValue DESC")
    List<Voucher> findValidVouchersForOrder(@Param("currentTime") LocalDateTime currentTime, 
                                           @Param("orderValue") Double orderValue);
    
    // Kiểm tra voucher có tồn tại và hợp lệ không
    @Query("SELECT COUNT(v) > 0 FROM Voucher v WHERE v.code = :code " +
           "AND v.isActive = true " +
           "AND (v.startDate IS NULL OR v.startDate <= :currentTime) " +
           "AND (v.endDate IS NULL OR v.endDate >= :currentTime) " +
           "AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage) " +
           "AND (v.minOrderValue IS NULL OR v.minOrderValue <= :orderValue)")
    boolean isValidVoucher(@Param("code") String code, 
                          @Param("currentTime") LocalDateTime currentTime, 
                          @Param("orderValue") Double orderValue);
    
    // Tìm voucher theo trạng thái active và thời gian
    @Query("SELECT v FROM Voucher v WHERE v.isActive = :isActive " +
           "AND (v.startDate IS NULL OR v.startDate <= :currentTime) " +
           "AND (v.endDate IS NULL OR v.endDate >= :currentTime)")
    List<Voucher> findByActiveAndTimeRange(@Param("isActive") Boolean isActive, 
                                          @Param("currentTime") LocalDateTime currentTime);
    
    // Đếm số voucher đang hoạt động
    long countByIsActiveTrue();
    
    // Đếm số voucher sắp hết hạn (trong vòng 7 ngày)
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.isActive = true " +
           "AND v.endDate BETWEEN :currentTime AND :expiryTime")
    long countExpiringSoon(@Param("currentTime") LocalDateTime currentTime, 
                          @Param("expiryTime") LocalDateTime expiryTime);
    
    // Tìm voucher theo loại giảm giá
    List<Voucher> findByDiscountTypeAndIsActiveTrue(Voucher.DiscountType discountType);
    
    // Tìm voucher có số lượt sử dụng gần hết
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND v.maxUsage IS NOT NULL " +
           "AND v.currentUsage >= (v.maxUsage * 0.8) " +
           "AND v.currentUsage < v.maxUsage")
    List<Voucher> findVouchersNearUsageLimit();

    List<Voucher> findAllByCodeIn(List<String> codes);
       List<Voucher> findByUserId(Long userId);


}