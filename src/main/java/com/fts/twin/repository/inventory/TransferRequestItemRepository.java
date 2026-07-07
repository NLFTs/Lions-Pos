package com.fts.twin.repository.inventory;

import com.fts.twin.model.inventory.TransferRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestItemRepository extends JpaRepository<TransferRequestItem, Long> {

    List<TransferRequestItem> findByTransferRequestId(Long transferRequestId);
}