package com.example.demo.repository;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ReferenceDataRepository {
    private final Map<String, List<String>> referenceData = new HashMap<>();

    public ReferenceDataRepository() {
        initializeReferenceData();
    }

    private void initializeReferenceData() {
        referenceData.put("POLICY_TERM", Arrays.asList("10", "15", "20", "25", "30"));
        referenceData.put("PAYMENT_FREQUENCY", Arrays.asList("MONTHLY", "QUARTERLY", "HALF_YEARLY", "ANNUAL"));
    }

    public List<String> findByCategory(String category) {
        return referenceData.getOrDefault(category, new ArrayList<>());
    }

    public boolean isValidPaymentFrequency(String frequency) {
        return referenceData.get("PAYMENT_FREQUENCY").contains(frequency);
    }

    public boolean isValidPolicyTerm(Integer term) {
        return referenceData.get("POLICY_TERM").contains(String.valueOf(term));
    }
}
