package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.TransferRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestItemRepository extends JpaRepository<TransferRequestItem, Long> {

    List<TransferRequestItem> findByTransferRequestId(Long transferRequestId);
}