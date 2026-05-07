package com.dak.spravel.repository.inventory;

import com.dak.spravel.model.inventory.StockOpnameItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockOpnameItemRepository extends JpaRepository<StockOpnameItem, Long> {

    List<StockOpnameItem> findByStockOpnameId(Long stockOpnameId);
}