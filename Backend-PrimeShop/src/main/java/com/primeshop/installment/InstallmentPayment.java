package com.primeshop.installment;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="installment_payments")
@Data
public class InstallmentPayment {
  @Id @GeneratedValue private Long id;
  @ManyToOne Installment installment;
  private Long amount;
  private Instant paidAt;
  private String vnpayTxnRef;
  private String status; // SUCCESS, FAILED
}
