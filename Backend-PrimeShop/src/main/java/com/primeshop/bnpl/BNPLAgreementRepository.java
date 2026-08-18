package com.primeshop.bnpl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BNPLAgreementRepository extends JpaRepository<BNPLAgreement, Long> {
    Optional<BNPLAgreement> findByOrderId(Long orderId);
    Long countByStatus(String status);
}
