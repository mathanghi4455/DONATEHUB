package com.example.donatehub_server.repository;

import com.example.donatehub_server.model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Optional<Recipient> findByOrganizationNameIgnoreCase(String organizationName);

    List<Recipient> findByOrganizationNameContainingIgnoreCase(String name);

    List<Recipient> findAllByOrderByCreatedAtDesc();
}
