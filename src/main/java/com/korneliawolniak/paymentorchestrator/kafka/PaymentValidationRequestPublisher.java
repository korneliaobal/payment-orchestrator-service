package com.korneliawolniak.paymentorchestrator.kafka;

import com.korneliawolniak.paymentprocessing.avro.PaymentValidationRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidationRequestPublisher {

  private static final String TOPIC = "payment-validation-request";

  private final KafkaTemplate<String, PaymentValidationRequest> kafkaTemplate;

  public PaymentValidationRequestPublisher(
      KafkaTemplate<String, PaymentValidationRequest> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publish(PaymentValidationRequest event) {
    kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
  }
}
