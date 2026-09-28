package com.example.donatehub_server.service;

import com.example.donatehub_server.model.Drive;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Backward compatibility alias delegating to DriveService
 */
@Service
public class DonationDriveService {

    private final DriveService driveService;

    public DonationDriveService(DriveService driveService) {
        this.driveService = driveService;
    }

    public Drive createDrive(Drive drive) {
        return driveService.createDrive(drive);
    }

    public List<Drive> getAllDrives() {
        return driveService.getAllDrives();
    }

    public Drive getDriveById(Long id) {
        return driveService.getDriveById(id);
    }

    public Map<String, Object> getDriveSummary(Long id) {
        return driveService.getDriveSummary(id);
    }
}
