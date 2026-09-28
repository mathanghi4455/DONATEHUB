package com.example.donatehub_server.service;

import com.example.donatehub_server.exception.InvalidOperationException;
import com.example.donatehub_server.exception.ResourceNotFoundException;
import com.example.donatehub_server.model.Donor;
import com.example.donatehub_server.repository.DonorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DonorService {

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public Donor createDonor(Donor donor) {
        if (donor.getName() == null || donor.getName().trim().isEmpty()) {
            throw new InvalidOperationException("Donor name is required");
        }
        donor.setName(donor.getName().trim());
        return donorRepository.save(donor);
    }

    @Transactional(readOnly = true)
    public List<Donor> getAllDonors() {
        return donorRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Donor getDonorById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + id));
    }

    public Donor updateDonor(Long id, Donor data) {
        Donor donor = getDonorById(id);
        if (data.getName() != null && !data.getName().trim().isEmpty()) donor.setName(data.getName().trim());
        if (data.getEmail() != null)       donor.setEmail(data.getEmail().trim());
        if (data.getPhoneNumber() != null) donor.setPhoneNumber(data.getPhoneNumber().trim());
        if (data.getAddress() != null)     donor.setAddress(data.getAddress().trim());
        return donorRepository.save(donor);
    }

    public void deleteDonor(Long id) {
        if (!donorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Donor not found with ID: " + id);
        }
        donorRepository.deleteById(id);
    }
}
