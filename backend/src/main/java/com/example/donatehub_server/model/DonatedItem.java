package com.example.donatehub_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: DonatedItem
 * Represents a physical item donated to a specific Drive by a specific Donor.
 *
 * Relationships:
 *   MANY DonatedItems → ONE Drive       (FK: drive_id)
 *   MANY DonatedItems → ONE Donor       (FK: donor_id)
 *   ONE  DonatedItem  → MANY DistributionRecords
 */
@Entity
@Table(name = "donated_items")
public class DonatedItem {

    /* Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* FK → Drive: each item belongs to exactly one drive */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "drive_id", nullable = false)
    private Drive drive;

    /* FK → Donor: each item is donated by exactly one donor */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    /* Core Item Fields */
    @Column(nullable = false, length = 150)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemCondition itemCondition;

    /** Total units donated (cannot change after logging) */
    @Column(nullable = false)
    private Integer totalQuantity = 1;

    /** Units still available in stock (decreases with each distribution) */
    @Column(nullable = false)
    private Integer availableQuantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Column(nullable = false)
    private LocalDate receivedDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /* Relationships */

    /** One DonatedItem → Many DistributionRecords (partial distributions allowed) */
    @OneToMany(mappedBy = "donatedItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DistributionRecord> distributions = new ArrayList<>();

    public DonatedItem() {}

    public DonatedItem(Drive drive, Donor donor, String itemName, ItemCategory category,
                       ItemCondition itemCondition, Integer quantity,
                       LocalDate receivedDate, String notes) {
        this.drive = drive;
        this.donor = donor;
        this.itemName = itemName;
        this.category = category;
        this.itemCondition = itemCondition;
        int qty = (quantity != null && quantity > 0) ? quantity : 1;
        this.totalQuantity = qty;
        this.availableQuantity = qty;
        this.status = ItemStatus.AVAILABLE;
        this.receivedDate = receivedDate != null ? receivedDate : LocalDate.now();
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    /** Recalculates status based on available vs total quantity */
    public void updateStatusFromQuantities() {
        if (availableQuantity == null || availableQuantity <= 0) {
            this.status = ItemStatus.DISTRIBUTED;
            this.availableQuantity = 0;
        } else if (availableQuantity.equals(totalQuantity)) {
            this.status = ItemStatus.AVAILABLE;
        } else {
            this.status = ItemStatus.PARTIALLY_DISTRIBUTED;
        }
    }

    /** How many units have been distributed so far */
    public int getDistributedQuantity() {
        int total = totalQuantity != null ? totalQuantity : 0;
        int avail = availableQuantity != null ? availableQuantity : 0;
        return Math.max(0, total - avail);
    }

    // ---- Getters and Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Drive getDrive() { return drive; }
    public void setDrive(Drive drive) { this.drive = drive; }

    public Donor getDonor() { return donor; }
    public void setDonor(Donor donor) { this.donor = donor; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public ItemCategory getCategory() { return category; }
    public void setCategory(ItemCategory category) { this.category = category; }

    public ItemCondition getItemCondition() { return itemCondition; }
    public void setItemCondition(ItemCondition itemCondition) { this.itemCondition = itemCondition; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
        updateStatusFromQuantities();
    }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DistributionRecord> getDistributions() { return distributions; }
    public void setDistributions(List<DistributionRecord> distributions) { this.distributions = distributions; }
}
