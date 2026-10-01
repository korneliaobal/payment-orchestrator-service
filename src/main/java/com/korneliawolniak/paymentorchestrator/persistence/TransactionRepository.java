package com.korneliawolniak.paymentorchestrator.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

  List<TransactionEntity> findByPaymentId(UUID paymentId);
}
