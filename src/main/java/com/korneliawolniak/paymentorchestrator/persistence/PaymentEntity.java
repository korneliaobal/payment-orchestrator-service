package com.korneliawolniak.paymentorchestrator.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
public class PaymentEntity {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  private PaymentStatus status;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentValidationStatus;

  private String debtorName;
  private String debtorAccountNumber;
  private String currency;

  @Column(precision = 38, scale = 8)
  private BigDecimal totalAmount;

  private Integer transactionCount;
  private Instant createdAt;

  @Convert(converter = ReasonCodesConverter.class)
  @Column(length = 1024)
  private List<String> reasonCodes = List.of();

  public List<String> getReasonCodes() {
    return reasonCodes == null ? List.of() : reasonCodes;
  }

  public void setReasonCodes(List<String> value) {
    reasonCodes = List.copyOf(value);
  }

  public String getDebtorName() {
    return debtorName;
  }

  public void setDebtorName(String value) {
    debtorName = value;
  }

  public String getDebtorAccountNumber() {
    return debtorAccountNumber;
  }

  public void setDebtorAccountNumber(String value) {
    debtorAccountNumber = value;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String value) {
    currency = value;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal value) {
    totalAmount = value;
  }

  public Integer getTransactionCount() {
    return transactionCount;
  }

  public void setTransactionCount(Integer value) {
    transactionCount = value;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant value) {
    createdAt = value;
  }

  protected PaymentEntity() {}

  public PaymentEntity(UUID id, PaymentStatus status, PaymentStatus paymentValidationStatus) {
    this.id = id;
    this.status = status;
    this.paymentValidationStatus = paymentValidationStatus;
  }

  public UUID getId() {
    return id;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public PaymentStatus getPaymentValidationStatus() {
    return paymentValidationStatus;
  }

  public void setPaymentValidationStatus(PaymentStatus paymentValidationStatus) {
    this.paymentValidationStatus = paymentValidationStatus;
  }
}
