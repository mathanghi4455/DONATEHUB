package com.example.donatehub_server.service;

import com.example.donatehub_server.exception.InvalidOperationException;
import com.example.donatehub_server.exception.ResourceNotFoundException;
import com.example.donatehub_server.model.Recipient;
import com.example.donatehub_server.repository.RecipientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RecipientService {

    private final RecipientRepository recipientRepository;

    public RecipientService(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    public Recipient createRecipient(Recipient recipient) {
        if (recipient.getOrganizationName() == null || recipient.getOrganizationName().trim().isEmpty()) {
            throw new InvalidOperationException("Organization name is required");
        }
        recipient.setOrganizationName(recipient.getOrganizationName().trim());
        return recipientRepository.save(recipient);
    }

    @Transactional(readOnly = true)
    public List<Recipient> getAllRecipients() {
        return recipientRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Recipient getRecipientById(Long id) {
        return recipientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with ID: " + id));
    }

    public Recipient updateRecipient(Long id, Recipient data) {
        Recipient r = getRecipientById(id);
        if (data.getOrganizationName() != null && !data.getOrganizationName().trim().isEmpty())
            r.setOrganizationName(data.getOrganizationName().trim());
        if (data.getContactPerson() != null)  r.setContactPerson(data.getContactPerson().trim());
        if (data.getContactEmail() != null)   r.setContactEmail(data.getContactEmail().trim());
        if (data.getPhoneNumber() != null)    r.setPhoneNumber(data.getPhoneNumber().trim());
        if (data.getAddress() != null)        r.setAddress(data.getAddress().trim());
        return recipientRepository.save(r);
    }

    public void deleteRecipient(Long id) {
        if (!recipientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recipient not found with ID: " + id);
        }
        recipientRepository.deleteById(id);
    }
}
