package com.example.donatehub_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Recipient
 * Represents a charitable organization that receives distributed items.
 *
 * Relationships:
 *   ONE Recipient → MANY DistributionRecords  (a recipient can receive items from multiple distributions)
 */
@Entity
@Table(name = "recipients")
public class Recipient {

    /* Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Core Fields */
    @Column(nullable = false, length = 150)
    private String organizationName;

    @Column(length = 100)
    private String contactPerson;

    @Column(length = 120)
    private String contactEmail;

    @Column(length = 30)
    private String phoneNumber;

    @Column(length = 250)
    private String address;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /* Relationships */

    /** One Recipient receives many DistributionRecords */
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DistributionRecord> distributions = new ArrayList<>();

    public Recipient() {}

    public Recipient(String organizationName, String contactPerson, String contactEmail, String phoneNumber, String address) {
        this.organizationName = organizationName;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DistributionRecord> getDistributions() { return distributions; }
    public void setDistributions(List<DistributionRecord> distributions) { this.distributions = distributions; }
}
