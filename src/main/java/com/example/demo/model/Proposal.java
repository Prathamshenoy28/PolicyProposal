package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proposal {
    private String proposalId;
    private String customerId;
    private Integer policyTerm;
    private Long sumAssured;
    private Long annualPremium;
    private String paymentFrequency;
    private String nomineeName;
    private String status; // DRAFT, SUBMITTED
    private String policyNumber;
    private Long createdAt;
    private Long submittedAt;
}
