package com.korneliawolniak.paymentorchestrator.dto;

import com.korneliawolniak.paymentorchestrator.persistence.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentHistoryItem(
    UUID paymentId,
    PaymentStatus status,
    PartyResponse debtor,
    String currency,
    BigDecimal totalAmount,
    Integer transactionCount,
    Instant createdAt) {}
