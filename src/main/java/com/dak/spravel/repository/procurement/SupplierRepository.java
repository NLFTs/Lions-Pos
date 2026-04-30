package com.dak.spravel.repository.procurement;

import com.dak.spravel.model.common.Partners;

import java.util.List;
import java.util.UUID;

public interface SupplierRepository {

    List<Partners> findByRole(String role);

    List<Partners> findByRoleAndIsActiveTrue(String role);

    List<Partners> findByNameContainingIgnoreCaseAndRole(String name, String role);

}
