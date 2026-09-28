package com.example.donatehub_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Drive
 * Represents a donation collection campaign with a defined time window.
 *
 * Relationships:
 *   ONE Drive → MANY DonatedItems   (a drive collects many items)
 *   ONE Drive → MANY DistributionRecords (distributions are logged per drive)
 */
@Entity
@Table(name = "drives")
public class Drive {

    /* Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Core Fields */
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DriveStatus status = DriveStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /* Relationships */

    /** One Drive has many DonatedItems */
    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DonatedItem> donatedItems = new ArrayList<>();

    /** One Drive has many DistributionRecords */
    @OneToMany(mappedBy = "drive", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DistributionRecord> distributions = new ArrayList<>();

    public Drive() {}

    public Drive(String name, String description, LocalDate startDate, LocalDate endDate, String location, DriveStatus status) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.status = status != null ? status : DriveStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public DriveStatus getStatus() { return status; }
    public void setStatus(DriveStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DonatedItem> getDonatedItems() { return donatedItems; }
    public void setDonatedItems(List<DonatedItem> donatedItems) { this.donatedItems = donatedItems; }

    public List<DistributionRecord> getDistributions() { return distributions; }
    public void setDistributions(List<DistributionRecord> distributions) { this.distributions = distributions; }
}
