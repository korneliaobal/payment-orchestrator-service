package com.korneliawolniak.paymentorchestrator.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
public class TransactionEntity {

  @Id private UUID id;

  private UUID paymentId;

  @Enumerated(EnumType.STRING)
  private PaymentStatus status;

  private String creditorName;
  private String creditorAccountNumber;

  @Column(precision = 38, scale = 8)
  private BigDecimal amount;

  @Convert(converter = ReasonCodesConverter.class)
  @Column(length = 1024)
  private List<String> reasonCodes = List.of();

  public List<String> getReasonCodes() {
    return reasonCodes == null ? List.of() : reasonCodes;
  }

  public void setReasonCodes(List<String> value) {
    reasonCodes = List.copyOf(value);
  }

  public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String value) {
    creditorName = value;
  }

  public String getCreditorAccountNumber() {
    return creditorAccountNumber;
  }

  public void setCreditorAccountNumber(String value) {
    creditorAccountNumber = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    amount = value;
  }

  protected TransactionEntity() {}

  public TransactionEntity(UUID id, UUID paymentId, PaymentStatus status) {
    this.id = id;
    this.paymentId = paymentId;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPaymentId() {
    return paymentId;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }
}
