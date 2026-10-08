package com.korneliawolniak.paymentorchestrator.dto;

import java.util.List;

public record PaymentHistoryResponse(
    List<PaymentHistoryItem> content, long totalElements, int totalPages, int number, int size) {}
