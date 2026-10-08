package com.korneliawolniak.paymentorchestrator.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.korneliawolniak.paymentorchestrator.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

class PaymentHistoryControllerTest {
  private final PaymentRepository payments = mock(PaymentRepository.class);
  private final PaymentHistoryController controller = new PaymentHistoryController(payments);

  @Test
  void returnsFilteredPaginatedPersistedData() {
    var payment = new PaymentEntity(UUID.randomUUID(), PaymentStatus.NOT_OK, PaymentStatus.OK);
    payment.setDebtorName("Portfolio sender");
    payment.setCurrency("PLN");
    payment.setTotalAmount(new BigDecimal("3.00"));
    payment.setTransactionCount(1);
    payment.setCreatedAt(Instant.parse("2026-10-07T12:00:00Z"));
    var page = PageRequest.of(1, 20);
    when(payments.findHistory(PaymentStatus.NOT_OK, page))
        .thenReturn(new PageImpl<>(List.of(payment), page, 21));
    var response = controller.history(1, 20, PaymentStatus.NOT_OK);
    assertEquals(21, response.totalElements());
    assertEquals(2, response.totalPages());
    assertEquals(1, response.number());
    assertEquals("Portfolio sender", response.content().getFirst().debtor().name());
    assertEquals(new BigDecimal("3.00"), response.content().getFirst().totalAmount());
  }

  @Test
  void preservesMissingMetadataForLegacyRecords() {
    var payment = new PaymentEntity(UUID.randomUUID(), PaymentStatus.OK, PaymentStatus.OK);
    when(payments.findHistory(null, PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(List.of(payment)));
    var row = controller.history(0, 20, null).content().getFirst();
    assertNull(row.createdAt());
    assertNull(row.totalAmount());
    assertNull(row.debtor().name());
  }

  @Test
  void rejectsUnboundedOrInvalidPages() {
    assertThrows(ResponseStatusException.class, () -> controller.history(-1, 20, null));
    assertThrows(ResponseStatusException.class, () -> controller.history(0, 0, null));
    assertThrows(ResponseStatusException.class, () -> controller.history(0, 101, null));
    verifyNoInteractions(payments);
  }
}
