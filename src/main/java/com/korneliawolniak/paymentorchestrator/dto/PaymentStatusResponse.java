package com.korneliawolniak.paymentorchestrator.dto;

import com.korneliawolniak.paymentorchestrator.persistence.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentStatusResponse(
    UUID paymentId,
    PaymentStatus status,
    PaymentStatus paymentValidationStatus,
    List<String> reasonCodes,
    PartyResponse debtor,
    String currency,
    BigDecimal totalAmount,
    Integer transactionCount,
    Instant createdAt,
    List<TransactionStatusResponse> transactions) {}
