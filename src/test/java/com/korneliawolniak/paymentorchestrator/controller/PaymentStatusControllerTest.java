package com.korneliawolniak.paymentorchestrator.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.korneliawolniak.paymentorchestrator.persistence.PaymentEntity;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentRepository;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentStatus;
import com.korneliawolniak.paymentorchestrator.persistence.TransactionEntity;
import com.korneliawolniak.paymentorchestrator.persistence.TransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentStatusControllerTest {
  private final PaymentRepository payments = mock(PaymentRepository.class);
  private final TransactionRepository transactions = mock(TransactionRepository.class);
  private final PaymentStatusController controller =
      new PaymentStatusController(payments, transactions);

  @Test
  void returnsNotFoundUntilPaymentIsPersisted() {
    UUID id = UUID.randomUUID();
    when(payments.findById(id)).thenReturn(Optional.empty());
    assertEquals(404, controller.getStatus(id).getStatusCode().value());
  }

  @Test
  void returnsPersistedPaymentAndTransactionStatuses() {
    UUID id = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    var payment = new PaymentEntity(id, PaymentStatus.NOT_OK, PaymentStatus.OK);
    payment.setCurrency("PLN");
    when(payments.findById(id)).thenReturn(Optional.of(payment));
    var transaction = new TransactionEntity(transactionId, id, PaymentStatus.NOT_OK);
    transaction.setReasonCodes(List.of("AMOUNT_BELOW_MINIMUM"));
    transaction.setCreditorName("Anna Kowalska");
    transaction.setAmount(new java.math.BigDecimal("3.00"));
    when(transactions.findByPaymentId(id)).thenReturn(List.of(transaction));
    var response = controller.getStatus(id);
    assertEquals(200, response.getStatusCode().value());
    assertEquals(PaymentStatus.NOT_OK, response.getBody().status());
    assertEquals(PaymentStatus.OK, response.getBody().paymentValidationStatus());
    assertEquals(transactionId, response.getBody().transactions().getFirst().transactionId());
    assertEquals(PaymentStatus.NOT_OK, response.getBody().transactions().getFirst().status());
    assertEquals(
        List.of("AMOUNT_BELOW_MINIMUM"),
        response.getBody().transactions().getFirst().reasonCodes());
    assertEquals("Anna Kowalska", response.getBody().transactions().getFirst().creditor().name());
    assertEquals("PLN", response.getBody().currency());
  }
}
