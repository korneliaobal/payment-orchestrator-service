package com.korneliawolniak.paymentorchestrator.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
  @Query(
      "select p from PaymentEntity p where (:status is null or p.status = :status) order by p.createdAt desc nulls last, p.id desc")
  Page<PaymentEntity> findHistory(PaymentStatus status, Pageable pageable);
}
