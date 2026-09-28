package com.example.donatehub_server.controller;

import com.example.donatehub_server.model.DonatedItem;
import com.example.donatehub_server.service.DonatedItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class DonatedItemController {

    private final DonatedItemService itemService;

    public DonatedItemController(DonatedItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Feature: Log a donated item with category, condition, donor, and drive.
     * Accepts driveId and donorId as query params, or extracts them from the item body if populated.
     */
    @PostMapping
    public ResponseEntity<DonatedItem> logItem(
            @RequestParam(required = false) Long driveId,
            @RequestParam(required = false) Long donorId,
            @RequestBody DonatedItem item) {

        Long targetDriveId = driveId;
        if (targetDriveId == null && item.getDrive() != null) {
            targetDriveId = item.getDrive().getId();
        }

        Long targetDonorId = donorId;
        if (targetDonorId == null && item.getDonor() != null) {
            targetDonorId = item.getDonor().getId();
        }

        DonatedItem created = itemService.logItem(targetDriveId, targetDonorId, item);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /** List all donated items */
    @GetMapping
    public ResponseEntity<List<DonatedItem>> getAllItems(
            @RequestParam(required = false) Long driveId,
            @RequestParam(required = false) Long donorId) {
        if (driveId != null) {
            return ResponseEntity.ok(itemService.getItemsByDrive(driveId));
        }
        if (donorId != null) {
            return ResponseEntity.ok(itemService.getItemsByDonor(donorId));
        }
        return ResponseEntity.ok(itemService.getAllItems());
    }

    /**
     * Feature: List undistributed items in remaining stock (availableQuantity > 0)
     */
    @GetMapping("/undistributed")
    public ResponseEntity<List<DonatedItem>> getUndistributedItems(@RequestParam(required = false) Long driveId) {
        if (driveId != null) {
            return ResponseEntity.ok(itemService.getUndistributedItemsByDrive(driveId));
        }
        return ResponseEntity.ok(itemService.getUndistributedItems());
    }

    /** Overall inventory and distribution summary */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getOverallSummary() {
        return ResponseEntity.ok(itemService.getOverallSummary());
    }

    /** Get item by ID */
    @GetMapping("/{id}")
    public ResponseEntity<DonatedItem> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    /** Update item details */
    @PutMapping("/{id}")
    public ResponseEntity<DonatedItem> updateItem(@PathVariable Long id, @RequestBody DonatedItem item) {
        return ResponseEntity.ok(itemService.updateItem(id, item));
    }

    /** Delete item */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(Map.of("message", "Donated item deleted successfully", "id", String.valueOf(id)));
    }
}
