package com.korneliawolniak.paymentorchestrator.controller;

import com.korneliawolniak.paymentorchestrator.dto.PartyResponse;
import com.korneliawolniak.paymentorchestrator.dto.PaymentHistoryItem;
import com.korneliawolniak.paymentorchestrator.dto.PaymentHistoryResponse;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentRepository;
import com.korneliawolniak.paymentorchestrator.persistence.PaymentStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PaymentHistoryController {
  private final PaymentRepository payments;

  public PaymentHistoryController(PaymentRepository payments) {
    this.payments = payments;
  }

  @GetMapping("/api/payment-history")
  public PaymentHistoryResponse history(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) PaymentStatus status) {
    if (page < 0 || size < 1 || size > 100)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be between 1 and 100");
    var result = payments.findHistory(status, PageRequest.of(page, size));
    var rows =
        result.map(
            payment ->
                new PaymentHistoryItem(
                    payment.getId(),
                    payment.getStatus(),
                    new PartyResponse(payment.getDebtorName(), payment.getDebtorAccountNumber()),
                    payment.getCurrency(),
                    payment.getTotalAmount(),
                    payment.getTransactionCount(),
                    payment.getCreatedAt()));
    return new PaymentHistoryResponse(
        rows.getContent(),
        rows.getTotalElements(),
        rows.getTotalPages(),
        rows.getNumber(),
        rows.getSize());
  }
}
