package com.korneliawolniak.paymentorchestrator.kafka;

import com.korneliawolniak.paymentprocessing.avro.PaymentValidationResult;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidationResultPublisher {

  private static final String TOPIC = "payment-validation-result";

  private final KafkaTemplate<String, PaymentValidationResult> kafkaTemplate;

  public PaymentValidationResultPublisher(
      KafkaTemplate<String, PaymentValidationResult> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publish(PaymentValidationResult event) {
    kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
  }
}
