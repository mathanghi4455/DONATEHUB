package com.example.donatehub_server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Donor
 * Represents a person who donates items to a drive.
 *
 * Relationships:
 *   ONE Donor → MANY DonatedItems   (a donor can donate many items across drives)
 */
@Entity
@Table(name = "donors")
public class Donor {

    /* Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Core Fields */
    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120, unique = true)
    private String email;

    @Column(length = 30)
    private String phoneNumber;

    @Column(length = 250)
    private String address;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /* Relationships */

    /** One Donor donates many DonatedItems */
    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DonatedItem> donatedItems = new ArrayList<>();

    public Donor() {}

    public Donor(String name, String email, String phoneNumber, String address) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    // ---- Getters and Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<DonatedItem> getDonatedItems() { return donatedItems; }
    public void setDonatedItems(List<DonatedItem> donatedItems) { this.donatedItems = donatedItems; }
}
