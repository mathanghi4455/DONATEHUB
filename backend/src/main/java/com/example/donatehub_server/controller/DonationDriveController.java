package com.example.donatehub_server.controller;

import com.example.donatehub_server.model.Drive;
import com.example.donatehub_server.service.DriveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Backward compatibility alias for /api/donation-drives -> DriveService
 */
@RestController
@RequestMapping("/api/donation-drives")
@CrossOrigin(origins = "*")
public class DonationDriveController {

    private final DriveService driveService;

    public DonationDriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping
    public ResponseEntity<Drive> createDrive(@RequestBody Drive drive) {
        return new ResponseEntity<>(driveService.createDrive(drive), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Drive>> getAllDrives() {
        return ResponseEntity.ok(driveService.getAllDrives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drive> getDriveById(@PathVariable Long id) {
        return ResponseEntity.ok(driveService.getDriveById(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getDriveSummary(@PathVariable Long id) {
        return ResponseEntity.ok(driveService.getDriveSummary(id));
    }
}
