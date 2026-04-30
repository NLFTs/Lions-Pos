package com.dak.spravel.repository.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dak.spravel.model.common.Partners;

@Repository
public interface PartnerRepository extends JpaRepository<Partners, Long> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
}
