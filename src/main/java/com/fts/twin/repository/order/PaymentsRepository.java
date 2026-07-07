package com.fts.twin.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fts.twin.model.order.Payments;

@Repository
public interface PaymentsRepository extends JpaRepository <Payments, Long> {
}
