package com.bookverse.bookCatalog.Controller;

import com.bookverse.bookCatalog.Models.InventoryAlert;
import com.bookverse.bookCatalog.Service.InventoryAlertService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-alerts")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "Inventory Alerts", description = "Inventory alert management operations")
public class InventoryAlertController {

    private final InventoryAlertService inventoryAlertService;

    public InventoryAlertController(InventoryAlertService inventoryAlertService) {
        this.inventoryAlertService = inventoryAlertService;
    }
    
    // Retrieves all inventory alerts.
    @GetMapping
    public List<InventoryAlert> getAllAlerts() {
        return inventoryAlertService.getAllAlerts();
    }

    // Retrieves a single alert by ID.
    @GetMapping("/{id}")
    public ResponseEntity<InventoryAlert> getAlertById(@PathVariable Long id) {
        return inventoryAlertService.getAlertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Retrieves an alert for a specific book.
    @GetMapping("/book/{bookId}")
    public ResponseEntity<InventoryAlert> getAlertByBookId(@PathVariable Long bookId) {
        return inventoryAlertService.getAlertByBookId(bookId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Creates or updates an inventory alert.
    @PostMapping
    public ResponseEntity<InventoryAlert> createOrUpdateAlert(@RequestBody InventoryAlert alert) {
        return new ResponseEntity<>(inventoryAlertService.saveAlert(alert), HttpStatus.CREATED);
    }

    // Deletes an inventory alert.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        inventoryAlertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}