package com.korneliawolniak.paymentorchestrator.controller;

import com.korneliawolniak.paymentorchestrator.dto.PartyResponse;
import com.korneliawolniak.paymentorchestrator.dto.PaymentStatusResponse;
import com.korneliawolniak.paymentorchestrator.dto.TransactionStatusResponse;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentRepository;
import com.korneliawolniak.paymentorchestrator.persistence.TransactionRepository;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "https://obal-flow.up.railway.app"})
@RequestMapping("/api/payment-status")
public class PaymentStatusController {
  private final PaymentRepository payments;
  private final TransactionRepository transactions;

  public PaymentStatusController(PaymentRepository payments, TransactionRepository transactions) {
    this.payments = payments;
    this.transactions = transactions;
  }

  @GetMapping("/{paymentId}")
  public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable UUID paymentId) {
    return payments
        .findById(paymentId)
        .map(
            payment ->
                ResponseEntity.ok(
                    new PaymentStatusResponse(
                        payment.getId(),
                        payment.getAuthorization().getStatus(),
                        payment.getAuthorization().getPaymentValidationStatus(),
                        payment.getReasonCodes(),
                        new PartyResponse(
                            payment.getDebtorName(), payment.getDebtorAccountNumber()),
                        payment.getCurrency(),
                        payment.getTotalAmount(),
                        payment.getTransactionCount(),
                        payment.getCreatedAt(),
                        transactions.findByPaymentId(paymentId).stream()
                            .map(
                                transaction ->
                                    new TransactionStatusResponse(
                                        transaction.getId(),
                                        transaction.getAuthorization().getStatus(),
                                        transaction.getAuthorization().getReasonCodes(),
                                        new PartyResponse(
                                            transaction.getCreditorName(),
                                            transaction.getCreditorAccountNumber()),
                                        transaction.getAmount()))
                            .toList())))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
