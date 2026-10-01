package com.korneliawolniak.paymentorchestrator.orchestrator;

import com.korneliawolniak.paymentorchestrator.kafka.PaymentValidationRequestPublisher;
import com.korneliawolniak.paymentorchestrator.kafka.TransactionValidationRequestPublisher;
import com.korneliawolniak.paymentorchestrator.mapper.PaymentValidationRequestMapper;
import com.korneliawolniak.paymentorchestrator.mapper.TransactionValidationRequestMapper;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentEntity;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentRepository;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentStatus;
import com.korneliawolniak.paymentorchestrator.persistence.TransactionEntity;
import com.korneliawolniak.paymentorchestrator.persistence.TransactionRepository;
import com.korneliawolniak.paymentorchestrator.service.PaymentStatusAggregator;
import com.korneliawolniak.paymentprocessing.avro.PaymentCreatedEvent;
import com.korneliawolniak.paymentprocessing.avro.PaymentValidationRequest;
import com.korneliawolniak.paymentprocessing.avro.PaymentValidationResult;
import com.korneliawolniak.paymentprocessing.avro.TransactionEvent;
import com.korneliawolniak.paymentprocessing.avro.TransactionValidationRequest;
import com.korneliawolniak.paymentprocessing.avro.TransactionValidationResult;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrchestrator {

  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentOrchestrator.class);

  private final PaymentRepository paymentRepository;
  private final TransactionRepository transactionRepository;
  private final PaymentValidationRequestMapper paymentValidationRequestMapper;
  private final PaymentValidationRequestPublisher paymentValidationRequestPublisher;
  private final TransactionValidationRequestMapper transactionValidationRequestMapper;
  private final TransactionValidationRequestPublisher transactionValidationRequestPublisher;
  private final PaymentStatusAggregator paymentStatusAggregator;

  public PaymentOrchestrator(
      PaymentRepository paymentRepository,
      TransactionRepository transactionRepository,
      PaymentValidationRequestMapper paymentValidationRequestMapper,
      PaymentValidationRequestPublisher paymentValidationRequestPublisher,
      TransactionValidationRequestMapper transactionValidationRequestMapper,
      TransactionValidationRequestPublisher transactionValidationRequestPublisher,
      PaymentStatusAggregator paymentStatusAggregator) {
    this.paymentRepository = paymentRepository;
    this.transactionRepository = transactionRepository;
    this.paymentValidationRequestMapper = paymentValidationRequestMapper;
    this.paymentValidationRequestPublisher = paymentValidationRequestPublisher;
    this.transactionValidationRequestMapper = transactionValidationRequestMapper;
    this.transactionValidationRequestPublisher = transactionValidationRequestPublisher;
    this.paymentStatusAggregator = paymentStatusAggregator;
  }

  @KafkaListener(topics = "payment-created", groupId = "payment-orchestrator")
  public void handlePaymentCreated(PaymentCreatedEvent event) {

    UUID paymentId = UUID.fromString(event.getPaymentId().toString());

    PaymentEntity paymentEntity =
        new PaymentEntity(paymentId, PaymentStatus.PENDING, PaymentStatus.PENDING);

    paymentRepository.save(paymentEntity);

    for (TransactionEvent transaction : event.getTransactions()) {

      UUID transactionId = UUID.fromString(transaction.getTransactionId().toString());

      TransactionEntity transactionEntity =
          new TransactionEntity(transactionId, paymentId, PaymentStatus.PENDING);

      transactionRepository.save(transactionEntity);
    }

    PaymentValidationRequest paymentValidationRequest =
        paymentValidationRequestMapper.toEvent(event);

    paymentValidationRequestPublisher.publish(paymentValidationRequest);

    for (TransactionEvent transaction : event.getTransactions()) {

      TransactionValidationRequest transactionValidationRequest =
          transactionValidationRequestMapper.toEvent(transaction, event.getCurrency());

      transactionValidationRequestPublisher.publish(transactionValidationRequest);
    }

    LOGGER.info("Started validation for payment {}", paymentId);
  }

  @KafkaListener(topics = "payment-validation-result", groupId = "payment-orchestrator")
  public void handlePaymentValidationResult(PaymentValidationResult result) {

    UUID paymentId = UUID.fromString(result.getPaymentId().toString());

    PaymentEntity payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));

    PaymentStatus status = PaymentStatus.valueOf(result.getStatus().toString());

    payment.setPaymentValidationStatus(status);

    paymentRepository.save(payment);

    LOGGER.info("Updated payment validation status: {} to {}", paymentId, status);

    paymentStatusAggregator.updateFinalPaymentStatus(paymentId);
  }

  @KafkaListener(topics = "transaction-validation-result", groupId = "payment-orchestrator")
  public void handleTransactionValidationResult(TransactionValidationResult result) {

    UUID transactionId = UUID.fromString(result.getTransactionId().toString());

    UUID paymentId = UUID.fromString(result.getPaymentId().toString());

    TransactionEntity transaction =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(
                () -> new IllegalStateException("Transaction not found: " + transactionId));

    PaymentStatus status = PaymentStatus.valueOf(result.getStatus().toString());

    transaction.setStatus(status);

    transactionRepository.save(transaction);

    LOGGER.info("Updated transaction validation status: {} to {}", transactionId, status);

    paymentStatusAggregator.updateFinalPaymentStatus(paymentId);
  }
}
