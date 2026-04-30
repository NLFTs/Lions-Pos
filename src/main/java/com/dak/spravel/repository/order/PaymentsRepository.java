package com.dak.spravel.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dak.spravel.model.order.Payments;

@Repository
public interface PaymentsRepository extends JpaRepository <Payments, Long> {
    
}
