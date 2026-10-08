package com.korneliawolniak.paymentorchestrator.service;

import com.korneliawolniak.paymentorchestrator.persistence.PaymentEntity;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentRepository;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentStatus;
import com.korneliawolniak.paymentorchestrator.persistence.TransactionEntity;
import com.korneliawolniak.paymentorchestrator.persistence.TransactionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentStatusAggregator {

  private final PaymentRepository paymentRepository;
  private final TransactionRepository transactionRepository;

  public PaymentStatusAggregator(
      PaymentRepository paymentRepository, TransactionRepository transactionRepository) {
    this.paymentRepository = paymentRepository;
    this.transactionRepository = transactionRepository;
  }

  @Transactional
  public void updateFinalPaymentStatus(UUID paymentId) {
    PaymentEntity payment =
        paymentRepository
            .findByIdForUpdate(paymentId)
            .orElseThrow(() -> new IllegalStateException("Payment not found: " + paymentId));

    List<TransactionEntity> transactions = transactionRepository.findByPaymentId(paymentId);

    if (payment.getAuthorization().getPaymentValidationStatus() == PaymentStatus.NOT_OK) {
      payment.getAuthorization().setStatus(PaymentStatus.NOT_OK);
      paymentRepository.save(payment);
      return;
    }

    boolean anyTransactionNotOk =
        transactions.stream()
            .anyMatch(
                transaction -> transaction.getAuthorization().getStatus() == PaymentStatus.NOT_OK);

    if (anyTransactionNotOk) {
      payment.getAuthorization().setStatus(PaymentStatus.NOT_OK);
      paymentRepository.save(payment);
      return;
    }

    boolean paymentValidationFinished =
        payment.getAuthorization().getPaymentValidationStatus() == PaymentStatus.OK;

    boolean allTransactionsOk =
        !transactions.isEmpty()
            && transactions.stream()
                .allMatch(
                    transaction -> transaction.getAuthorization().getStatus() == PaymentStatus.OK);

    if (paymentValidationFinished && allTransactionsOk) {
      payment.getAuthorization().setStatus(PaymentStatus.OK);
      paymentRepository.save(payment);
    }
  }
}
