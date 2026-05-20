package com.dak.spravel.repository.inventory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dak.spravel.model.inventory.TransferRequest;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

    List<TransferRequest> findByPartnerId(Long partnerId);

    List<TransferRequest> findByStatus(TransferRequest.Status status);

    List<TransferRequest> findByPartnerIdAndStatus(Long partnerId, TransferRequest.Status status);

    List<TransferRequest> findByDeletedAtIsNull();

    List<TransferRequest> findByPartnerIdAndDeletedAtIsNull(Long partnerId);

    Page<TransferRequest> findByPartnerIdAndDeletedAtIsNull(Long partnerId, Pageable pageable);
}