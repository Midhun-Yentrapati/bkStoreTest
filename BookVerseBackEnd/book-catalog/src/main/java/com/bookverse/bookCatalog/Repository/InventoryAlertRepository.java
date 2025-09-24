package com.bookverse.bookCatalog.Repository;

import com.bookverse.bookCatalog.Models.InventoryAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Long> {
    Optional<InventoryAlert> findByBookId(Long bookId);
    List<InventoryAlert> findByCurrentStockLessThanEqual(int threshold);
    List<InventoryAlert> findByAlertTypeAndIsResolvedFalse(InventoryAlert.AlertType alertType);
}