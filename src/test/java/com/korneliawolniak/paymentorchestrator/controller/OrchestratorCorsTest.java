package com.korneliawolniak.paymentorchestrator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:orchestrator-cors;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.kafka.listener.auto-startup=false",
      "spring.jpa.show-sql=false"
    })
@AutoConfigureMockMvc
class OrchestratorCorsTest {
  @Autowired private MockMvc mvc;

  @Test
  void allowsLocalFrontendToReadHistory() throws Exception {
    mvc.perform(get("/api/payment-history").header(HttpHeaders.ORIGIN, "http://localhost:4200"))
        .andExpect(status().isOk())
        .andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
  }

  @Test
  void allowsStatusPreflightAndExposesMissingPaymentResponse() throws Exception {
    String path = "/api/payment-status/" + UUID.randomUUID();
    mvc.perform(
            options(path)
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isOk())
        .andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    mvc.perform(get(path).header(HttpHeaders.ORIGIN, "http://localhost:4200"))
        .andExpect(status().isNotFound())
        .andExpect(
            header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
  }

  @Test
  void rejectsAnUnconfiguredFrontendOrigin() throws Exception {
    mvc.perform(
            options("/api/payment-history")
                .header(HttpHeaders.ORIGIN, "https://unconfigured.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }
}
