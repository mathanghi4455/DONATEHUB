package com.example.donatehub_server.controller;

import com.example.donatehub_server.model.DistributionRecord;
import com.example.donatehub_server.service.DistributionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/distributions")
@CrossOrigin(origins = "*")
public class DistributionController {

    private final DistributionService distributionService;

    public DistributionController(DistributionService distributionService) {
        this.distributionService = distributionService;
    }

    /**
     * Feature: Record distribution of items to a recipient organization.
     * Accepts either URL query params or a JSON request body.
     */
    @PostMapping
    public ResponseEntity<DistributionRecord> recordDistribution(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String distributedBy,
            @RequestParam(required = false) String distributionDate,
            @RequestBody(required = false) Map<String, Object> body) {

        Long targetItemId = itemId;
        Long targetRecipientId = recipientId;
        Integer targetQty = quantity;
        String targetPurpose = purpose;
        String targetBy = distributedBy;
        LocalDate targetDate = distributionDate != null && !distributionDate.isEmpty()
                ? LocalDate.parse(distributionDate) : null;

        if (body != null) {
            if (targetItemId == null && body.containsKey("itemId")) {
                targetItemId = Long.valueOf(body.get("itemId").toString());
            }
            if (targetRecipientId == null && body.containsKey("recipientId")) {
                targetRecipientId = Long.valueOf(body.get("recipientId").toString());
            }
            if (targetQty == null && body.containsKey("quantity")) {
                targetQty = Integer.valueOf(body.get("quantity").toString());
            }
            if (targetPurpose == null && body.containsKey("purpose")) {
                targetPurpose = (String) body.get("purpose");
            }
            if (targetBy == null && body.containsKey("distributedBy")) {
                targetBy = (String) body.get("distributedBy");
            }
            if (targetDate == null && body.containsKey("distributionDate") && body.get("distributionDate") != null) {
                targetDate = LocalDate.parse(body.get("distributionDate").toString());
            }
        }

        DistributionRecord record = distributionService.recordDistribution(
                targetItemId,
                targetRecipientId,
                targetQty,
                targetPurpose,
                targetBy,
                targetDate
        );

        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }

    /** List all distribution records */
    @GetMapping
    public ResponseEntity<List<DistributionRecord>> getAllDistributions(
            @RequestParam(required = false) Long driveId,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long recipientId) {

        if (driveId != null) {
            return ResponseEntity.ok(distributionService.getDistributionsByDrive(driveId));
        }
        if (itemId != null) {
            return ResponseEntity.ok(distributionService.getDistributionsByItem(itemId));
        }
        if (recipientId != null) {
            return ResponseEntity.ok(distributionService.getDistributionsByRecipient(recipientId));
        }
        return ResponseEntity.ok(distributionService.getAllDistributions());
    }

    /** Get distribution record by ID */
    @GetMapping("/{id}")
    public ResponseEntity<DistributionRecord> getDistributionById(@PathVariable Long id) {
        return ResponseEntity.ok(distributionService.getDistributionById(id));
    }
}
