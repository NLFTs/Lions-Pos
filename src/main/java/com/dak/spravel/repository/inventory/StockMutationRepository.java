package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.model.inventory.StockMutation.Location;
import com.dak.spravel.model.inventory.StockMutation.ReferenceType;
import com.dak.spravel.model.inventory.StockMutation.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.dak.spravel.model.common.Partners;

@Repository
public interface StockMutationRepository extends JpaRepository<StockMutation, Long> {

    List<StockMutation> findByProductId(Long productId);

    List<StockMutation> findByPartner(Partners partner, Sort sort);

    Page<StockMutation> findByPartnerIdOrderByCreatedAtDesc(Long partnerId, Pageable pageable);

    List<StockMutation> findByType(Type type);

    List<StockMutation> findByFromLocationTypeAndFromLocationId(Location fromLocationType, Long fromLocationId);

    List<StockMutation> findByToLocationTypeAndToLocationId(Location toLocationType, Long toLocationId);

    List<StockMutation> findByReferenceTypeAndReferenceId(ReferenceType referenceType, Long referenceId);
}