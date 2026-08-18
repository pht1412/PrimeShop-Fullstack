package com.primeshop.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import com.primeshop.user.User;

public interface OrderRepo extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findById(Long id);

    List<Order> findByUser(User user);

    List<Order> findBySellerId(Long sellerId);

    Order findByPaypalPaymentId(String paypalOrderId);

    @Query("SELECT COUNT(o) FROM Order o")
    Long countByOrder();

    // @Query("SELECT SUM(o.totalAmount) FROM Order o where o.createdAt between :startDate and :endDate and o.status = :DELIVERED")
    // BigDecimal getTotalIncome(Date startDate, Date endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o where o.status = :status and o.user.id = :userId")
    BigDecimal getTotalPurchase(OrderStatus status, Long userId);

    @Query("SELECT COUNT(o) FROM Order o where o.user.id = :userId")
    Long countByUser(Long userId);

    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}
