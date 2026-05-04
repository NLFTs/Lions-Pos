package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.TransferRequest;
import com.dak.spravel.model.inventory.TransferRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

    List<TransferRequest> findByPartnerId(Long partnerId);

    List<TransferRequest> findByStatus(Status status);

    List<TransferRequest> findByPartnerIdAndStatus(Long partnerId, Status status);

    List<TransferRequest> findByFromLocationTypeAndFromLocationId(String fromLocationType, Long fromLocationId);

    List<TransferRequest> findByToLocationTypeAndToLocationId(String toLocationType, Long toLocationId);

    List<TransferRequest> findByDeletedAtIsNull();

    List<TransferRequest> findByPartnerIdAndDeletedAtIsNull(Long partnerId);

    List<TransferRequest> findByStatusAndDeletedAtIsNull(Status status);


}
