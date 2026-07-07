package com.fts.twin.repository.inventory;

import com.fts.twin.model.common.Partners;
import com.fts.twin.model.inventory.Branches;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BranchesRepository extends JpaRepository<Branches, Long> {

    List<Branches> findByPartners(Partners partner);

    Page<Branches> findByPartnersId(Long partnerId, Pageable pageable);

    List<Branches> findByIsActive(Boolean isActive);

}