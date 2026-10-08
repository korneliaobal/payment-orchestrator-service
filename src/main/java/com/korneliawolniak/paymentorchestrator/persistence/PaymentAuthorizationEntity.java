package com.korneliawolniak.paymentorchestrator.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "payment_authorizations")
public class PaymentAuthorizationEntity {
  @Id
  @Column(name = "payment_id")
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "payment_id", nullable = false)
  private PaymentEntity payment;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus paymentValidationStatus;

  protected PaymentAuthorizationEntity() {}

  public PaymentAuthorizationEntity(
      PaymentEntity payment, PaymentStatus status, PaymentStatus paymentValidationStatus) {
    this.payment = payment;
    this.status = status;
    this.paymentValidationStatus = paymentValidationStatus;
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

  public PaymentStatus getPaymentValidationStatus() {
    return paymentValidationStatus;
  }

  public void setPaymentValidationStatus(PaymentStatus value) {
    paymentValidationStatus = value;
  }
}
