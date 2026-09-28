package com.example.donatehub_server.service;

import com.example.donatehub_server.exception.InvalidOperationException;
import com.example.donatehub_server.exception.ResourceNotFoundException;
import com.example.donatehub_server.model.DonatedItem;
import com.example.donatehub_server.model.Donor;
import com.example.donatehub_server.model.Drive;
import com.example.donatehub_server.model.ItemStatus;
import com.example.donatehub_server.repository.DonatedItemRepository;
import com.example.donatehub_server.repository.DonorRepository;
import com.example.donatehub_server.repository.DriveRepository;
import com.example.donatehub_server.repository.DistributionRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DonatedItemService {

    private final DonatedItemRepository itemRepository;
    private final DriveRepository driveRepository;
    private final DonorRepository donorRepository;
    private final DistributionRecordRepository distributionRepository;

    public DonatedItemService(DonatedItemRepository itemRepository,
                              DriveRepository driveRepository,
                              DonorRepository donorRepository,
                              DistributionRecordRepository distributionRepository) {
        this.itemRepository = itemRepository;
        this.driveRepository = driveRepository;
        this.donorRepository = donorRepository;
        this.distributionRepository = distributionRepository;
    }

    /**
     * Log a donated item for a specific drive and donor.
     * driveId and donorId are required FKs.
     */
    public DonatedItem logItem(Long driveId, Long donorId, DonatedItem item) {
        if (driveId == null) throw new InvalidOperationException("driveId is required");
        if (donorId == null) throw new InvalidOperationException("donorId is required");
        if (item.getItemName() == null || item.getItemName().trim().isEmpty())
            throw new InvalidOperationException("itemName is required");
        if (item.getCategory() == null)
            throw new InvalidOperationException("category is required (CLOTHING, FOOD, BOOKS, TOYS, MEDICAL_SUPPLIES, BEDDING, HYGIENE_PRODUCTS, ELECTRONICS, OTHER)");
        if (item.getItemCondition() == null)
            throw new InvalidOperationException("itemCondition is required (NEW, LIKE_NEW, GOOD, FAIR)");

        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with ID: " + driveId));
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + donorId));

        // Bug fix: reject invalid quantity instead of silently defaulting to 1
        if (item.getTotalQuantity() == null || item.getTotalQuantity() <= 0) {
            throw new InvalidOperationException("totalQuantity must be a positive number (got: " + item.getTotalQuantity() + ")");
        }
        int qty = item.getTotalQuantity();

        item.setDrive(drive);
        item.setDonor(donor);
        item.setItemName(item.getItemName().trim());
        item.setTotalQuantity(qty);
        item.setAvailableQuantity(qty);
        item.setStatus(ItemStatus.AVAILABLE);
        if (item.getReceivedDate() == null) item.setReceivedDate(LocalDate.now());

        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<DonatedItem> getAllItems() {
        return itemRepository.findAllByOrderByReceivedDateDesc();
    }

    @Transactional(readOnly = true)
    public DonatedItem getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donated item not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DonatedItem> getItemsByDrive(Long driveId) {
        if (!driveRepository.existsById(driveId))
            throw new ResourceNotFoundException("Drive not found with ID: " + driveId);
        return itemRepository.findByDriveIdOrderByReceivedDateDesc(driveId);
    }

    @Transactional(readOnly = true)
    public List<DonatedItem> getItemsByDonor(Long donorId) {
        if (!donorRepository.existsById(donorId))
            throw new ResourceNotFoundException("Donor not found with ID: " + donorId);
        return itemRepository.findByDonorIdOrderByReceivedDateDesc(donorId);
    }

    /** List undistributed items in remaining stock */
    @Transactional(readOnly = true)
    public List<DonatedItem> getUndistributedItems() {
        return itemRepository.findByAvailableQuantityGreaterThanOrderByReceivedDateDesc(0);
    }

    /** List undistributed items in remaining stock for a specific drive */
    @Transactional(readOnly = true)
    public List<DonatedItem> getUndistributedItemsByDrive(Long driveId) {
        if (!driveRepository.existsById(driveId))
            throw new ResourceNotFoundException("Drive not found with ID: " + driveId);
        return itemRepository.findByDriveIdAndAvailableQuantityGreaterThanOrderByReceivedDateDesc(driveId, 0);
    }

    public DonatedItem updateItem(Long id, DonatedItem data) {
        DonatedItem item = getItemById(id);
        if (data.getItemName() != null && !data.getItemName().trim().isEmpty()) item.setItemName(data.getItemName().trim());
        if (data.getCategory() != null)      item.setCategory(data.getCategory());
        if (data.getItemCondition() != null) item.setItemCondition(data.getItemCondition());
        if (data.getNotes() != null)         item.setNotes(data.getNotes().trim());
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id))
            throw new ResourceNotFoundException("Donated item not found with ID: " + id);
        itemRepository.deleteById(id);
    }

    /** Overall inventory summary across all drives */
    @Transactional(readOnly = true)
    public Map<String, Object> getOverallSummary() {
        long totalDrives     = driveRepository.count();
        long totalItems      = itemRepository.count();
        int  totalCollected  = itemRepository.sumTotalQuantityAll();
        int  totalDistributed= distributionRepository.sumDistributedQuantityAll();
        int  totalRemaining  = itemRepository.sumAvailableQuantityAll();
        long undistributed   = itemRepository.countUndistributedItems();
        double rate = totalCollected > 0
                ? BigDecimal.valueOf((double) totalDistributed / totalCollected * 100)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        Map<String, Object> s = new LinkedHashMap<>();
        s.put("totalDrives",             totalDrives);
        s.put("totalItemEntries",        totalItems);
        s.put("totalUnitsCollected",     totalCollected);
        s.put("totalUnitsDistributed",   totalDistributed);
        s.put("totalRemainingStock",     totalRemaining);
        s.put("distributionPercentage",  rate);
        s.put("undistributedItemEntries",undistributed);
        return s;
    }
}
