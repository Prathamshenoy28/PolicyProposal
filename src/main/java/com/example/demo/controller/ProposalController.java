package com.example.demo.controller;

import com.example.demo.dto.ProposalRequest;
import com.example.demo.model.Proposal;
import com.example.demo.service.ProposalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proposals")
public class ProposalController {
    @Autowired
    private ProposalService proposalService;

    @PostMapping
    public ResponseEntity<Proposal> createProposal(@Valid @RequestBody ProposalRequest request) {
        Proposal response = proposalService.createProposal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proposal> getProposal(@PathVariable String id) {
        Proposal proposal = proposalService.getProposal(id);
        return ResponseEntity.ok(proposal);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Proposal> submitProposal(@PathVariable String id) {
        Proposal response = proposalService.submitProposal(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Proposal>> getAllProposals() {
        List<Proposal> proposals = proposalService.getAllProposals();
        return ResponseEntity.ok(proposals);
    }
}
