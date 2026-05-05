package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.Branches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchesRepository extends JpaRepository<Branches, Long> {

    List<Branches> findByPartnersId(Long partnersId);

    List<Branches> findByIsActive(Boolean isActive);
}