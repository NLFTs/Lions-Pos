package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.StockMutation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMutationRepository extends JpaRepository<StockMutation,Long> {

    List<StockMutation> findByProductId(Long productId);

    List<StockMutation> findByPartnerId(Long partnerId);

    List<StockMutation> findByType(String type);

    List<StockMutation> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    List<StockMutation> findByFromLocationTypeAndFromLocationId(String fromLocationType, Long fromLocationId);

    List<StockMutation> findByToLocationTypeAndToLocationId(String toLocationType, Long toLocationId);
}