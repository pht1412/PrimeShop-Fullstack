package com.primeshop.installment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentRepo extends JpaRepository<Installment, Long> {
    List<Installment> findByAgreementIdOrderBySeqNo(Long agreementId);
  }
