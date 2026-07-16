package com.example.demo.service;

import com.example.demo.dto.ProposalRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Audit;
import com.example.demo.model.Customer;
import com.example.demo.model.Proposal;
import com.example.demo.repository.AuditRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.ProposalRepository;
import com.example.demo.repository.ReferenceDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProposalService {
    private final ProposalRepository proposalRepository;
    private final CustomerRepository customerRepository;
    private final AuditRepository auditRepository;
    private final ReferenceDataRepository referenceDataRepository;

    @Autowired
    public ProposalService(ProposalRepository proposalRepository,
                           CustomerRepository customerRepository,
                           AuditRepository auditRepository,
                           ReferenceDataRepository referenceDataRepository) {
        this.proposalRepository = proposalRepository;
        this.customerRepository = customerRepository;
        this.auditRepository = auditRepository;
        this.referenceDataRepository = referenceDataRepository;
    }

    public Proposal createProposal(ProposalRequest request) {
        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        // Business validations
        validateProposalInput(request, customer);

        String proposalId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        Proposal proposal = Proposal.builder()
                .proposalId(proposalId)
                .customerId(request.getCustomerId())
                .policyTerm(request.getPolicyTerm())
                .sumAssured(request.getSumAssured())
                .annualPremium(request.getAnnualPremium())
                .paymentFrequency(request.getPaymentFrequency())
                .nomineeName(request.getNomineeName())
                .status("DRAFT")
                .createdAt(currentTime)
                .build();

        proposalRepository.save(proposal);

        // Create audit record
        Audit audit = Audit.builder()
                .auditId(UUID.randomUUID().toString())
                .entityType("PROPOSAL")
                .entityId(proposalId)
                .action("CREATE")
                .details("Proposal created for customer: " + request.getCustomerId())
                .timestamp(currentTime)
                .build();
        auditRepository.save(audit);

        return proposal;
    }

    public Proposal getProposal(String proposalId) {
        return proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with ID: " + proposalId));
    }

    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll();
    }

    public Proposal submitProposal(String proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with ID: " + proposalId));

        // Validate proposal status
        if (!"DRAFT".equals(proposal.getStatus())) {
            throw new BusinessException("Only DRAFT proposals can be submitted");
        }

        // Get customer for additional validations
        Customer customer = customerRepository.findById(proposal.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Re-validate proposal data
        validateProposalInput(ProposalRequest.builder()
                .customerId(proposal.getCustomerId())
                .policyTerm(proposal.getPolicyTerm())
                .sumAssured(proposal.getSumAssured())
                .annualPremium(proposal.getAnnualPremium())
                .paymentFrequency(proposal.getPaymentFrequency())
                .nomineeName(proposal.getNomineeName())
                .build(), customer);

        // Generate unique policy number
        String policyNumber = generatePolicyNumber();

        long currentTime = System.currentTimeMillis();
        proposal.setStatus("SUBMITTED");
        proposal.setPolicyNumber(policyNumber);
        proposal.setSubmittedAt(currentTime);

        proposalRepository.save(proposal);

        // Create audit record for submission
        Audit audit = Audit.builder()
                .auditId(UUID.randomUUID().toString())
                .entityType("PROPOSAL")
                .entityId(proposalId)
                .action("SUBMIT")
                .details("Proposal submitted with policy number: " + policyNumber)
                .timestamp(currentTime)
                .build();
        auditRepository.save(audit);

        return proposal;
    }

    private void validateProposalInput(ProposalRequest request, Customer customer) {
        // Policy term validation
        if (!referenceDataRepository.isValidPolicyTerm(request.getPolicyTerm())) {
            throw new BusinessException("Policy term must be one of: 10, 15, 20, 25, 30 years");
        }

        // Sum assured validation
        if (request.getSumAssured() < 100000 || request.getSumAssured() > 50000000L) {
            throw new BusinessException("Sum assured must be between Rs. 1,00,000 and Rs. 5,00,00,000");
        }

        // Annual premium validation
        if (request.getAnnualPremium() < 5000) {
            throw new BusinessException("Annual premium must be at least Rs. 5,000");
        }

        // Payment frequency validation
        if (!referenceDataRepository.isValidPaymentFrequency(request.getPaymentFrequency())) {
            throw new BusinessException("Payment frequency must be one of: MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL");
        }

        // PAN validation - mandatory if annual premium exceeds Rs. 50,000
        if (request.getAnnualPremium() > 50000 && (customer.getPan() == null || customer.getPan().isEmpty())) {
            throw new BusinessException("PAN is mandatory when annual premium exceeds Rs. 50,000");
        }

        // Nominee validation - cannot be the same as customer
        String customerFullName = customer.getFirstName() + " " + customer.getLastName();
        if (request.getNomineeName().equalsIgnoreCase(customerFullName)) {
            throw new BusinessException("Nominee cannot be the same as the customer");
        }

        if (request.getNomineeName().isEmpty()) {
            throw new BusinessException("Nominee is mandatory");
        }
    }

    private String generatePolicyNumber() {
        return "POL" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
