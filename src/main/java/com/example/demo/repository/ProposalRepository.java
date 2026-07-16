package com.example.demo.repository;

import com.example.demo.model.Proposal;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProposalRepository {
    private final Map<String, Proposal> proposals = new HashMap<>();

    public Proposal save(Proposal proposal) {
        proposals.put(proposal.getProposalId(), proposal);
        return proposal;
    }

    public Optional<Proposal> findById(String proposalId) {
        return Optional.ofNullable(proposals.get(proposalId));
    }

    public List<Proposal> findAll() {
        return new ArrayList<>(proposals.values());
    }

    public List<Proposal> findByCustomerId(String customerId) {
        return proposals.values().stream()
                .filter(p -> p.getCustomerId().equals(customerId))
                .toList();
    }

    public boolean existsById(String proposalId) {
        return proposals.containsKey(proposalId);
    }
}
