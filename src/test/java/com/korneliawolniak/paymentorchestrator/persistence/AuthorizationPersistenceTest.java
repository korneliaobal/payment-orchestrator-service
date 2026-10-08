package com.korneliawolniak.paymentorchestrator.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.korneliawolniak.paymentorchestrator.kafka.PaymentValidationRequestPublisher;
import com.korneliawolniak.paymentorchestrator.kafka.TransactionValidationRequestPublisher;
import com.korneliawolniak.paymentorchestrator.orchestrator.PaymentOrchestrator;
import com.korneliawolniak.paymentprocessing.avro.PaymentCreatedEvent;
import com.korneliawolniak.paymentprocessing.avro.PaymentValidationResult;
import com.korneliawolniak.paymentprocessing.avro.TransactionEvent;
import com.korneliawolniak.paymentprocessing.avro.TransactionValidationResult;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:authorizations;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.kafka.listener.auto-startup=false",
      "spring.jpa.show-sql=false"
    })
@Transactional
class AuthorizationPersistenceTest {
  @Autowired private PaymentRepository payments;
  @Autowired private TransactionRepository transactions;
  @Autowired private EntityManager entityManager;
  @Autowired private PaymentOrchestrator orchestrator;
  @MockitoBean private PaymentValidationRequestPublisher paymentPublisher;
  @MockitoBean private TransactionValidationRequestPublisher transactionPublisher;

  @Test
  void persistsBothAuthorizationsAndFiltersHistoryByMovedStatus() {
    UUID paymentId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    payments.save(new PaymentEntity(paymentId, PaymentStatus.PENDING, PaymentStatus.PENDING));
    transactions.save(new TransactionEntity(transactionId, paymentId, PaymentStatus.PENDING));
    entityManager.flush();
    entityManager.clear();

    var payment = payments.findByIdForUpdate(paymentId).orElseThrow();
    var transaction = transactions.findById(transactionId).orElseThrow();
    assertEquals(paymentId, payment.getAuthorization().getId());
    assertEquals(transactionId, transaction.getAuthorization().getId());
    payment.getAuthorization().setPaymentValidationStatus(PaymentStatus.OK);
    payment.getAuthorization().setStatus(PaymentStatus.NOT_OK);
    transaction.getAuthorization().setStatus(PaymentStatus.NOT_OK);
    entityManager.flush();
    entityManager.clear();

    var page = payments.findHistory(PaymentStatus.NOT_OK, PageRequest.of(0, 20));
    assertEquals(1, page.getTotalElements());
    assertEquals(paymentId, page.getContent().getFirst().getId());
    assertEquals(
        PaymentStatus.OK,
        page.getContent().getFirst().getAuthorization().getPaymentValidationStatus());
    assertEquals(
        PaymentStatus.NOT_OK,
        transactions.findById(transactionId).orElseThrow().getAuthorization().getStatus());
    assertEquals(
        0, payments.findHistory(PaymentStatus.OK, PageRequest.of(0, 20)).getTotalElements());
  }

  @Test
  void handlesTransactionResultBeforePaymentResult() {
    UUID paymentId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    createPayment(paymentId, transactionId);
    orchestrator.handleTransactionValidationResult(
        new TransactionValidationResult(
            transactionId.toString(), paymentId.toString(), "OK", List.of()));
    assertEquals(
        PaymentStatus.PENDING,
        payments.findById(paymentId).orElseThrow().getAuthorization().getStatus());
    orchestrator.handlePaymentValidationResult(
        new PaymentValidationResult(paymentId.toString(), "OK", List.of()));
    entityManager.flush();
    entityManager.clear();
    assertEquals(
        PaymentStatus.OK,
        payments.findById(paymentId).orElseThrow().getAuthorization().getStatus());
    assertEquals(
        PaymentStatus.OK,
        transactions.findById(transactionId).orElseThrow().getAuthorization().getStatus());
  }

  @Test
  void keepsRejectionReasonsWhenPaymentResultArrivesFirst() {
    UUID paymentId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    createPayment(paymentId, transactionId);
    orchestrator.handlePaymentValidationResult(
        new PaymentValidationResult(paymentId.toString(), "OK", List.of()));
    orchestrator.handleTransactionValidationResult(
        new TransactionValidationResult(
            transactionId.toString(),
            paymentId.toString(),
            "NOT_OK",
            List.of("AMOUNT_BELOW_MINIMUM")));
    entityManager.flush();
    entityManager.clear();
    assertEquals(
        PaymentStatus.NOT_OK,
        payments.findById(paymentId).orElseThrow().getAuthorization().getStatus());
    var transaction = transactions.findById(transactionId).orElseThrow();
    assertEquals(PaymentStatus.NOT_OK, transaction.getAuthorization().getStatus());
    assertEquals(List.of("AMOUNT_BELOW_MINIMUM"), transaction.getAuthorization().getReasonCodes());
  }

  private void createPayment(UUID paymentId, UUID transactionId) {
    var transaction =
        new TransactionEvent(
            transactionId.toString(),
            paymentId.toString(),
            "Recipient",
            "PL10105000997603123456789123",
            "10.00");
    orchestrator.handlePaymentCreated(
        new PaymentCreatedEvent(
            paymentId.toString(),
            "Sender",
            "PL61109010140000071219812874",
            "PLN",
            1,
            "10.00",
            List.of(transaction)));
    entityManager.flush();
    entityManager.clear();
  }
}
