package com.korneliawolniak.paymentorchestrator.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class AuthorizationMigrationTest {
  @Test
  void movesExistingStatusesAndRemovesOriginalColumns() throws Exception {
    String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    try (var connection = DriverManager.getConnection(url, "sa", "");
        var statement = connection.createStatement();
        var baseline =
            getClass().getResourceAsStream("/db/migration/V1__baseline_payment_schema.sql")) {
      statement.execute(new String(baseline.readAllBytes(), StandardCharsets.UTF_8));
      var paymentId = UUID.randomUUID();
      var transactionId = UUID.randomUUID();
      statement.execute(
          "INSERT INTO payment_entity (id, status, payment_validation_status, debtor_name) VALUES ('"
              + paymentId
              + "', 'NOT_OK', 'OK', 'Saved sender')");
      statement.execute(
          "INSERT INTO transaction_entity (id, payment_id, status) VALUES ('"
              + transactionId
              + "', '"
              + paymentId
              + "', 'NOT_OK')");
      statement.execute(
          "UPDATE transaction_entity SET reason_codes = 'CREDITOR_ACCOUNT_INVALID,AMOUNT_BELOW_MINIMUM'");
      Flyway.configure()
          .dataSource(url, "sa", "")
          .baselineOnMigrate(true)
          .baselineVersion("1")
          .load()
          .migrate();
      try (var rows =
          statement.executeQuery(
              "SELECT p.debtor_name, a.status, a.payment_validation_status FROM payment_entity p JOIN payment_authorizations a ON a.payment_id = p.id")) {
        assertTrue(rows.next());
        assertEquals("Saved sender", rows.getString(1));
        assertEquals("NOT_OK", rows.getString(2));
        assertEquals("OK", rows.getString(3));
      }
      try (var rows =
          statement.executeQuery(
              "SELECT transaction_id, status, reason_codes FROM transaction_authorizations")) {
        assertTrue(rows.next());
        assertEquals(transactionId, rows.getObject(1, UUID.class));
        assertEquals("NOT_OK", rows.getString(2));
        assertEquals("CREDITOR_ACCOUNT_INVALID,AMOUNT_BELOW_MINIMUM", rows.getString(3));
      }
      try (var columns =
          connection.getMetaData().getColumns(null, null, "TRANSACTION_ENTITY", "REASON_CODES")) {
        assertFalse(columns.next());
      }
      for (String table : new String[] {"PAYMENT_ENTITY", "TRANSACTION_ENTITY"}) {
        try (var columns = connection.getMetaData().getColumns(null, null, table, "STATUS")) {
          assertFalse(columns.next());
        }
      }
      try (var columns =
          connection
              .getMetaData()
              .getColumns(null, null, "PAYMENT_ENTITY", "PAYMENT_VALIDATION_STATUS")) {
        assertFalse(columns.next());
      }
      assertEquals(
          0, Flyway.configure().dataSource(url, "sa", "").load().migrate().migrationsExecuted);
    }
  }

  @Test
  void preservesReasonsForTransactionsWithoutAnAuthorization() throws Exception {
    String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    Flyway.configure().dataSource(url, "sa", "").target("2").load().migrate();
    try (var connection = DriverManager.getConnection(url, "sa", "");
        var statement = connection.createStatement()) {
      var id = UUID.randomUUID();
      statement.execute(
          "INSERT INTO transaction_entity (id, reason_codes) VALUES ('"
              + id
              + "', 'AMOUNT_BELOW_MINIMUM')");
      Flyway.configure().dataSource(url, "sa", "").load().migrate();
      try (var rows =
          statement.executeQuery(
              "SELECT transaction_id, status, reason_codes FROM transaction_authorizations")) {
        assertTrue(rows.next());
        assertEquals(id, rows.getObject(1, UUID.class));
        assertEquals("PENDING", rows.getString(2));
        assertEquals("AMOUNT_BELOW_MINIMUM", rows.getString(3));
      }
    }
  }
}
