package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.StockMutation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.dak.spravel.model.common.Partners;

@Repository
public interface StockMutationRepository extends JpaRepository<StockMutation, Long> {

    List<StockMutation> findByProductId(Long productId);

    List<StockMutation> findByPartner(Partners partner);

    Page<StockMutation> findByPartnerId(Long partnerId, Pageable pageable);

    List<StockMutation> findByType(String type);

    List<StockMutation> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    List<StockMutation> findByFromLocationTypeAndFromLocationId(String fromLocationType, Long fromLocationId);

    List<StockMutation> findByToLocationTypeAndToLocationId(String toLocationType, Long toLocationId);
}