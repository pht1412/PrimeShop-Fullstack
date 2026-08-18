package com.primeshop.bnpl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BNPLPaymentRepository extends JpaRepository<BNPLPayment, Long> { }
