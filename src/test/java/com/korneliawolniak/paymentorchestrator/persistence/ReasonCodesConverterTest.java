package com.korneliawolniak.paymentorchestrator.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReasonCodesConverterTest {
  @Test
  void roundTripsMultipleReasonsAndReadsLegacyNulls() {
    var converter = new ReasonCodesConverter();
    var reasons = List.of("CREDITOR_ACCOUNT_INVALID", "AMOUNT_BELOW_MINIMUM");
    assertEquals(
        reasons, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(reasons)));
    assertEquals(List.of(), converter.convertToEntityAttribute(null));
    assertNull(converter.convertToDatabaseColumn(List.of()));
  }
}
