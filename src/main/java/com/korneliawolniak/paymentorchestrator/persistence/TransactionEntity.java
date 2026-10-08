package com.korneliawolniak.paymentorchestrator.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class TransactionEntity {

  @Id private UUID id;

  private UUID paymentId;

  @OneToOne(
      mappedBy = "transaction",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      optional = false)
  private TransactionAuthorizationEntity authorization;

  private String creditorName;
  private String creditorAccountNumber;

  @Column(precision = 38, scale = 8)
  private BigDecimal amount;

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
    this.authorization = new TransactionAuthorizationEntity(this, status);
  }

  public UUID getId() {
    return id;
  }

  public UUID getPaymentId() {
    return paymentId;
  }

  public TransactionAuthorizationEntity getAuthorization() {
    return authorization;
  }
}
