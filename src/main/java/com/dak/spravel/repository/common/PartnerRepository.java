package com.dak.spravel.repository.common;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;

@Repository
public interface PartnerRepository extends JpaRepository<Partners, Long> {
    List<Partners> findAllByCreatedBy(User user, Sort sort);
    List<Partners> findByDeletedAtIsNull(Sort sort);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    List<Partners> findByPlan(Partners.Plan plan);
}
