package com.primeshop.installment;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Table(name="installment_installments")
@Data
public class Installment {
  @Id 
  @GeneratedValue 
  private Long id;
  @ManyToOne @JoinColumn(name="agreement_id") private InstallmentAgreement agreement;
  private Integer seqNo;
  private LocalDate dueDate;
  private Long principal;
  private Long interest;
  private Long totalDue;
  private String status; // DUE, PAID
  @Version private Integer version; // optimistic locking
}
