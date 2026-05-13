package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchesRepository extends JpaRepository<Branches, Long> {

    List<Branches> findByPartners(Partners partner);

    Page<Branches> findByPartnersId(Long partnerId, Pageable pageable);

    List<Branches> findByIsActive(Boolean isActive);

}