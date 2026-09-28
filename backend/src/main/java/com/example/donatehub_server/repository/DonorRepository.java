package com.example.donatehub_server.repository;

import com.example.donatehub_server.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    Optional<Donor> findByEmailIgnoreCase(String email);

    List<Donor> findByNameContainingIgnoreCase(String name);

    List<Donor> findAllByOrderByCreatedAtDesc();
}
