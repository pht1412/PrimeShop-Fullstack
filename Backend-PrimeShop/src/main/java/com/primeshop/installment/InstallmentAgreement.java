package com.primeshop.installment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="installment_agreements")
@Data
public class InstallmentAgreement {
  @Id 
  @GeneratedValue 
  private Long id;
  private Long orderId;
  private Long userId;
  private Long amount; // in cents or smallest unit
  private Integer months;
  private BigDecimal annualRate; // percent like 12.5
  private String status; // INIT, PENDING, ACTIVE...
  private String referenceCode;
  private Instant createdAt;
  private Instant updatedAt;
  @OneToMany(mappedBy="agreement", cascade=CascadeType.ALL) 
  private List<Installment> installments = new ArrayList<>();
}
