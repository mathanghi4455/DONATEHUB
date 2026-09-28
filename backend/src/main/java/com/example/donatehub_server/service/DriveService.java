package com.example.donatehub_server.service;

import com.example.donatehub_server.exception.InvalidOperationException;
import com.example.donatehub_server.exception.ResourceNotFoundException;
import com.example.donatehub_server.model.Drive;
import com.example.donatehub_server.model.DriveStatus;
import com.example.donatehub_server.repository.DistributionRecordRepository;
import com.example.donatehub_server.repository.DonatedItemRepository;
import com.example.donatehub_server.repository.DriveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DriveService {

    private final DriveRepository driveRepository;
    private final DonatedItemRepository itemRepository;
    private final DistributionRecordRepository distributionRepository;

    public DriveService(DriveRepository driveRepository,
                        DonatedItemRepository itemRepository,
                        DistributionRecordRepository distributionRepository) {
        this.driveRepository = driveRepository;
        this.itemRepository = itemRepository;
        this.distributionRepository = distributionRepository;
    }

    public Drive createDrive(Drive drive) {
        if (drive.getName() == null || drive.getName().trim().isEmpty()) {
            throw new InvalidOperationException("Drive name is required");
        }
        if (drive.getStartDate() == null || drive.getEndDate() == null) {
            throw new InvalidOperationException("Both startDate and endDate are required");
        }
        if (drive.getEndDate().isBefore(drive.getStartDate())) {
            throw new InvalidOperationException("endDate cannot be before startDate");
        }
        if (drive.getStatus() == null) drive.setStatus(DriveStatus.ACTIVE);
        drive.setName(drive.getName().trim());
        return driveRepository.save(drive);
    }

    @Transactional(readOnly = true)
    public List<Drive> getAllDrives() {
        return driveRepository.findAllByOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public Drive getDriveById(Long id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with ID: " + id));
    }

    public Drive updateDrive(Long id, Drive data) {
        Drive drive = getDriveById(id);
        if (data.getName() != null && !data.getName().trim().isEmpty()) drive.setName(data.getName().trim());
        if (data.getDescription() != null) drive.setDescription(data.getDescription().trim());
        if (data.getLocation() != null)    drive.setLocation(data.getLocation().trim());
        if (data.getStatus() != null)      drive.setStatus(data.getStatus());
        if (data.getStartDate() != null)   drive.setStartDate(data.getStartDate());
        if (data.getEndDate() != null)     drive.setEndDate(data.getEndDate());
        if (drive.getEndDate().isBefore(drive.getStartDate())) {
            throw new InvalidOperationException("endDate cannot be before startDate");
        }
        return driveRepository.save(drive);
    }

    public void deleteDrive(Long id) {
        if (!driveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Drive not found with ID: " + id);
        }
        driveRepository.deleteById(id);
    }

    /** View total items collected and distributed per drive */
    @Transactional(readOnly = true)
    public Map<String, Object> getDriveSummary(Long id) {
        Drive drive = getDriveById(id);
        long  totalEntries    = itemRepository.countItemsByDriveId(id);
        int   totalCollected  = itemRepository.sumTotalQuantityByDriveId(id);
        int   totalDistributed= distributionRepository.sumDistributedQuantityByDriveId(id);
        int   remaining       = itemRepository.sumAvailableQuantityByDriveId(id);
        double pct = totalCollected > 0
                ? BigDecimal.valueOf((double) totalDistributed / totalCollected * 100)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("driveId",              drive.getId());
        summary.put("driveName",            drive.getName());
        summary.put("startDate",            drive.getStartDate());
        summary.put("endDate",              drive.getEndDate());
        summary.put("status",               drive.getStatus());
        summary.put("totalDonationEntries", totalEntries);
        summary.put("totalItemsCollected",  totalCollected);
        summary.put("totalItemsDistributed",totalDistributed);
        summary.put("remainingStock",       remaining);
        summary.put("distributionPercentage", pct);
        return summary;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllDrivesSummary() {
        return driveRepository.findAllByOrderByStartDateDesc()
                .stream()
                .map(d -> getDriveSummary(d.getId()))
                .collect(Collectors.toList());
    }
}
