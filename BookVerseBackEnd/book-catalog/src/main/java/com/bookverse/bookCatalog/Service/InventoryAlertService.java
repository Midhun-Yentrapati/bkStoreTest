package com.bookverse.bookCatalog.Service;

import com.bookverse.bookCatalog.Models.InventoryAlert;
import com.bookverse.bookCatalog.Repository.InventoryAlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryAlertService {

    private final InventoryAlertRepository inventoryAlertRepository;

    public InventoryAlertService(InventoryAlertRepository inventoryAlertRepository) {
        this.inventoryAlertRepository = inventoryAlertRepository;
    }

    // Retrieves a list of all inventory alerts.
    public List<InventoryAlert> getAllAlerts() {
        return inventoryAlertRepository.findAll();
    }
    
    // Finds an inventory alert by its ID.
    public Optional<InventoryAlert> getAlertById(Long id) {
        return inventoryAlertRepository.findById(id);
    }
    
    // Finds an alert for a specific book.
    public Optional<InventoryAlert> getAlertByBookId(Long bookId) {
        return inventoryAlertRepository.findByBookId(bookId);
    }

    // Creates or updates an inventory alert.
    public InventoryAlert saveAlert(InventoryAlert alert) {
        return inventoryAlertRepository.save(alert);
    }

    // Deletes an inventory alert.
    public void deleteAlert(Long id) {
        inventoryAlertRepository.deleteById(id);
    }
    
    // Finds all alerts that are for low stock and have not been resolved.
    public List<InventoryAlert> getUnresolvedLowStockAlerts() {
        return inventoryAlertRepository.findByAlertTypeAndIsResolvedFalse(InventoryAlert.AlertType.LOW_STOCK);
    }
}