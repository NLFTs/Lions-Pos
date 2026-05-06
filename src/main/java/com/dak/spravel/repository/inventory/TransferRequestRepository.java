package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

    List<TransferRequest> findByPartnerId(Long partnerId);

    List<TransferRequest> findByStatus(TransferRequest.Status status);

    List<TransferRequest> findByPartnerIdAndStatus(Long partnerId, TransferRequest.Status status);

    List<TransferRequest> findByDeletedAtIsNull();

    List<TransferRequest> findByPartnerIdAndDeletedAtIsNull(Long partnerId);
}