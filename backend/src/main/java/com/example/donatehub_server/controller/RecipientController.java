package com.example.donatehub_server.controller;

import com.example.donatehub_server.model.Recipient;
import com.example.donatehub_server.service.RecipientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipients")
@CrossOrigin(origins = "*")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @PostMapping
    public ResponseEntity<Recipient> createRecipient(@RequestBody Recipient recipient) {
        return new ResponseEntity<>(recipientService.createRecipient(recipient), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Recipient>> getAllRecipients() {
        return ResponseEntity.ok(recipientService.getAllRecipients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipient> getRecipientById(@PathVariable Long id) {
        return ResponseEntity.ok(recipientService.getRecipientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recipient> updateRecipient(@PathVariable Long id, @RequestBody Recipient recipient) {
        return ResponseEntity.ok(recipientService.updateRecipient(id, recipient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRecipient(@PathVariable Long id) {
        recipientService.deleteRecipient(id);
        return ResponseEntity.ok(Map.of("message", "Recipient deleted successfully", "id", String.valueOf(id)));
    }
}
