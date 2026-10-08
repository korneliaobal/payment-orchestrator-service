package com.korneliawolniak.paymentorchestrator.dto;

import com.korneliawolniak.paymentorchestrator.persistence.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransactionStatusResponse(
    UUID transactionId,
    PaymentStatus status,
    List<String> reasonCodes,
    PartyResponse creditor,
    BigDecimal amount) {}
