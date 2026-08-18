package com.primeshop.installment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentAgreementRepo extends JpaRepository<InstallmentAgreement, Long> {
    Optional<InstallmentAgreement> findByReferenceCode(String ref);
    Optional<InstallmentAgreement> findByOrderId(Long orderId);
  }
