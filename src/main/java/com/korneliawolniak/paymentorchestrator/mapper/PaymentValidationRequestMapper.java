package com.korneliawolniak.paymentorchestrator.mapper;

import com.korneliawolniak.paymentprocessing.avro.PaymentCreatedEvent;
import com.korneliawolniak.paymentprocessing.avro.PaymentValidationRequest;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidationRequestMapper {

  public PaymentValidationRequest toEvent(PaymentCreatedEvent event) {
    return PaymentValidationRequest.newBuilder()
        .setPaymentId(event.getPaymentId())
        .setDebtorName(event.getDebtorName())
        .setDebtorAccountNumber(event.getDebtorAccountNumber())
        .setCurrency(event.getCurrency())
        .setTransactionCount(event.getTransactionCount())
        .build();
  }
}
