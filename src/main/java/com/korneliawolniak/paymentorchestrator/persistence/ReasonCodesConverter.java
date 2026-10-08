package com.korneliawolniak.paymentorchestrator.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;

@Converter
public class ReasonCodesConverter implements AttributeConverter<List<String>, String> {
  public String convertToDatabaseColumn(List<String> value) {
    return value == null || value.isEmpty() ? null : String.join(",", value);
  }

  public List<String> convertToEntityAttribute(String value) {
    return value == null || value.isBlank() ? List.of() : Arrays.asList(value.split(","));
  }
}
