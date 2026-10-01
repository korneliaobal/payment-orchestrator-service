package com.korneliawolniak.paymentorchestrator.kafka;

import com.korneliawolniak.paymentprocessing.avro.PaymentCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentPublisher {

  private static final String TOPIC = "payment-created";

  private final KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate;

  public PaymentPublisher(KafkaTemplate<String, PaymentCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publish(PaymentCreatedEvent event) {
    kafkaTemplate.send(TOPIC, event.getPaymentId().toString(), event);
  }
}
