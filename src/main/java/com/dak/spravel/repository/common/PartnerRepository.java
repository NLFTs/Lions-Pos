package com.dak.spravel.repository.common;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.common.Partners;

@Repository
public interface PartnerRepository extends JpaRepository<Partners, Long> {
    Optional<Partners> findById(Long id);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    List<Partners> findByPlan(Partners.Plan plan);
}
