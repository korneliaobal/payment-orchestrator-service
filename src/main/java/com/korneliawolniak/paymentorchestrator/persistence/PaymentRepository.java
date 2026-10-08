package com.korneliawolniak.paymentorchestrator.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from PaymentEntity p where p.id = :id")
  Optional<PaymentEntity> findByIdForUpdate(UUID id);

  @EntityGraph(attributePaths = "authorization")
  @Query(
      "select p from PaymentEntity p where (:status is null or p.authorization.status = :status) order by p.createdAt desc nulls last, p.id desc")
  Page<PaymentEntity> findHistory(PaymentStatus status, Pageable pageable);
}
