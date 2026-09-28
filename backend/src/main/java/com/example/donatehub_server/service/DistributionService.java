package com.example.donatehub_server.service;

import com.example.donatehub_server.exception.InvalidOperationException;
import com.example.donatehub_server.exception.ResourceNotFoundException;
import com.example.donatehub_server.model.DistributionRecord;
import com.example.donatehub_server.model.DonatedItem;
import com.example.donatehub_server.model.Drive;
import com.example.donatehub_server.model.Recipient;
import com.example.donatehub_server.repository.DistributionRecordRepository;
import com.example.donatehub_server.repository.DonatedItemRepository;
import com.example.donatehub_server.repository.DriveRepository;
import com.example.donatehub_server.repository.RecipientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DistributionService {

    private final DistributionRecordRepository distributionRepository;
    private final DonatedItemRepository itemRepository;
    private final DriveRepository driveRepository;
    private final RecipientRepository recipientRepository;

    public DistributionService(DistributionRecordRepository distributionRepository,
                               DonatedItemRepository itemRepository,
                               DriveRepository driveRepository,
                               RecipientRepository recipientRepository) {
        this.distributionRepository = distributionRepository;
        this.itemRepository = itemRepository;
        this.driveRepository = driveRepository;
        this.recipientRepository = recipientRepository;
    }

    /**
     * Record distribution of items to a recipient organization.
     * Enforces remaining stock validation.
     */
    public DistributionRecord recordDistribution(Long itemId, Long recipientId,
                                                Integer quantity, String purpose,
                                                String distributedBy, LocalDate distributionDate) {
        if (itemId == null) {
            throw new InvalidOperationException("Donated item ID is required");
        }
        if (recipientId == null) {
            throw new InvalidOperationException("Recipient ID is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new InvalidOperationException("Distributed quantity must be greater than zero");
        }

        DonatedItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Donated item not found with ID: " + itemId));

        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with ID: " + recipientId));

        int available = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
        if (available <= 0) {
            throw new InvalidOperationException("Item '" + item.getItemName() + "' (ID: " + item.getId() + ") is already fully distributed. Remaining stock is 0.");
        }
        if (quantity > available) {
            throw new InvalidOperationException("Cannot distribute " + quantity + " units. Only " + available + " units are available in stock.");
        }

        // Deduct available quantity — setAvailableQuantity() calls updateStatusFromQuantities()
        // which automatically sets status to AVAILABLE / PARTIALLY_DISTRIBUTED / DISTRIBUTED
        item.setAvailableQuantity(available - quantity);
        itemRepository.save(item); // persist updated quantity and status

        Drive drive = item.getDrive();
        LocalDate recordDate = distributionDate != null ? distributionDate : LocalDate.now();

        DistributionRecord record = new DistributionRecord(
                drive,
                item,
                recipient,
                quantity,
                recordDate,
                purpose != null ? purpose.trim() : null,
                distributedBy != null ? distributedBy.trim() : null
        );

        return distributionRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<DistributionRecord> getAllDistributions() {
        return distributionRepository.findAllByOrderByDistributionDateDesc();
    }

    @Transactional(readOnly = true)
    public DistributionRecord getDistributionById(Long id) {
        return distributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distribution record not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DistributionRecord> getDistributionsByDrive(Long driveId) {
        if (!driveRepository.existsById(driveId)) {
            throw new ResourceNotFoundException("Drive not found with ID: " + driveId);
        }
        return distributionRepository.findByDriveIdOrderByDistributionDateDesc(driveId);
    }

    @Transactional(readOnly = true)
    public List<DistributionRecord> getDistributionsByItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Donated item not found with ID: " + itemId);
        }
        return distributionRepository.findByDonatedItemIdOrderByDistributionDateDesc(itemId);
    }

    @Transactional(readOnly = true)
    public List<DistributionRecord> getDistributionsByRecipient(Long recipientId) {
        if (!recipientRepository.existsById(recipientId)) {
            throw new ResourceNotFoundException("Recipient not found with ID: " + recipientId);
        }
        return distributionRepository.findByRecipientIdOrderByDistributionDateDesc(recipientId);
    }
}
