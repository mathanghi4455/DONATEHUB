package com.example.donatehub_server.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity: DistributionRecord
 * Records a single distribution event — items dispatched to a Recipient from a Drive.
 *
 * Relationships:
 *   MANY DistributionRecords → ONE Drive         (FK: drive_id)
 *   MANY DistributionRecords → ONE DonatedItem   (FK: item_id)
 *   MANY DistributionRecords → ONE Recipient     (FK: recipient_id)
 */
@Entity
@Table(name = "distribution_records")
public class DistributionRecord {

    /* Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* FK → Drive: which drive this distribution belongs to */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "drive_id", nullable = false)
    private Drive drive;

    /* FK → DonatedItem: the specific item being distributed */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private DonatedItem donatedItem;

    /* FK → Recipient: the organization receiving the items */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    /* Core Fields */

    /** Number of units dispatched in this single distribution event */
    @Column(nullable = false)
    private Integer distributedQuantity;

    @Column(nullable = false)
    private LocalDate distributionDate;

    @Column(length = 500)
    private String purpose;

    @Column(length = 100)
    private String distributedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DistributionRecord() {}

    public DistributionRecord(Drive drive, DonatedItem donatedItem, Recipient recipient,
                              Integer distributedQuantity, LocalDate distributionDate,
                              String purpose, String distributedBy) {
        this.drive = drive;
        this.donatedItem = donatedItem;
        this.recipient = recipient;
        this.distributedQuantity = distributedQuantity;
        this.distributionDate = distributionDate != null ? distributionDate : LocalDate.now();
        this.purpose = purpose;
        this.distributedBy = distributedBy;
        this.createdAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Drive getDrive() { return drive; }
    public void setDrive(Drive drive) { this.drive = drive; }

    public DonatedItem getDonatedItem() { return donatedItem; }
    public void setDonatedItem(DonatedItem donatedItem) { this.donatedItem = donatedItem; }

    public Recipient getRecipient() { return recipient; }
    public void setRecipient(Recipient recipient) { this.recipient = recipient; }

    public Integer getDistributedQuantity() { return distributedQuantity; }
    public void setDistributedQuantity(Integer distributedQuantity) { this.distributedQuantity = distributedQuantity; }

    public LocalDate getDistributionDate() { return distributionDate; }
    public void setDistributionDate(LocalDate distributionDate) { this.distributionDate = distributionDate; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getDistributedBy() { return distributedBy; }
    public void setDistributedBy(String distributedBy) { this.distributedBy = distributedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
