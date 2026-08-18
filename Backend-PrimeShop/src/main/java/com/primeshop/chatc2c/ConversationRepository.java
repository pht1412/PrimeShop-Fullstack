package com.primeshop.chatc2c;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findBySellerIdAndCustomerId(Long sellerId, Long customerId);
    List<Conversation> findByCustomerId(Long customerId);
    List<Conversation> findBySellerIdOrCustomerId(Long sellerId, Long customerId);

    // Tìm xem 2 người này đã chat chưa (cho hàm create)

    // --- THÊM HÀM NÀY ---
    // Tìm tất cả cuộc hội thoại của userId (dù là seller hay customer)
    // Sắp xếp tin nhắn mới nhất lên đầu (ORDER BY lastMessageAt DESC)
    @Query("SELECT c FROM Conversation c WHERE c.sellerId = :userId OR c.customerId = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByUserId(@Param("userId") Long userId);
}
