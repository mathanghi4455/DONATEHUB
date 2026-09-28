package com.example.donatehub_server.controller;

import com.example.donatehub_server.model.Drive;
import com.example.donatehub_server.service.DriveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drives")
@CrossOrigin(origins = "*")
public class DriveController {

    private final DriveService driveService;

    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    /** Feature: Create a donation drive with start and end dates */
    @PostMapping
    public ResponseEntity<Drive> createDrive(@RequestBody Drive drive) {
        Drive created = driveService.createDrive(drive);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /** List all donation drives */
    @GetMapping
    public ResponseEntity<List<Drive>> getAllDrives() {
        return ResponseEntity.ok(driveService.getAllDrives());
    }

    /** Get drive by ID */
    @GetMapping("/{id}")
    public ResponseEntity<Drive> getDriveById(@PathVariable Long id) {
        return ResponseEntity.ok(driveService.getDriveById(id));
    }

    /** Update a drive */
    @PutMapping("/{id}")
    public ResponseEntity<Drive> updateDrive(@PathVariable Long id, @RequestBody Drive drive) {
        return ResponseEntity.ok(driveService.updateDrive(id, drive));
    }

    /** Delete a drive */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDrive(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return ResponseEntity.ok(Map.of("message", "Drive deleted successfully", "id", String.valueOf(id)));
    }

    /** Feature: View total items collected and distributed per drive */
    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getDriveSummary(@PathVariable Long id) {
        return ResponseEntity.ok(driveService.getDriveSummary(id));
    }

    /** Summaries for all drives */
    @GetMapping("/summaries")
    public ResponseEntity<List<Map<String, Object>>> getAllDrivesSummary() {
        return ResponseEntity.ok(driveService.getAllDrivesSummary());
    }
}
