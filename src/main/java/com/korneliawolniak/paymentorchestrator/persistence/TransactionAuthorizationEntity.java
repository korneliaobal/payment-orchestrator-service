package com.korneliawolniak.paymentorchestrator.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transaction_authorizations")
public class TransactionAuthorizationEntity {
  @Id
  @Column(name = "transaction_id")
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transaction_id", nullable = false)
  private TransactionEntity transaction;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Convert(converter = ReasonCodesConverter.class)
  @Column(length = 1024)
  private List<String> reasonCodes = List.of();

  public List<String> getReasonCodes() {
    return reasonCodes == null ? List.of() : reasonCodes;
  }

  public void setReasonCodes(List<String> value) {
    reasonCodes = List.copyOf(value);
  }

  protected TransactionAuthorizationEntity() {}

  public TransactionAuthorizationEntity(TransactionEntity transaction, PaymentStatus status) {
    this.transaction = transaction;
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus value) {
    status = value;
  }
}
