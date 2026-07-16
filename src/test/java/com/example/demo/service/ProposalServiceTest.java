package com.example.demo.service;

import com.example.demo.dto.ProposalRequest;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.Customer;
import com.example.demo.model.Proposal;
import com.example.demo.repository.AuditRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.ProposalRepository;
import com.example.demo.repository.ReferenceDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProposalServiceTest {
    private ProposalService proposalService;
    private ProposalRepository proposalRepository;
    private CustomerRepository customerRepository;
    private AuditRepository auditRepository;
    private ReferenceDataRepository referenceDataRepository;

    @BeforeEach
    void setUp() {
        proposalRepository = new ProposalRepository();
        customerRepository = new CustomerRepository();
        auditRepository = new AuditRepository();
        referenceDataRepository = new ReferenceDataRepository();
        proposalService = new ProposalService(proposalRepository, customerRepository, auditRepository, referenceDataRepository);
    }

    private Customer createTestCustomer(String customerId, int age, String pan) {
        Customer customer = Customer.builder()
                .customerId(customerId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9999999999")
                .age(age)
                .pan(pan)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
        customerRepository.save(customer);
        return customer;
    }

    @Test
    void testProposalCreationSuccess() {
        // Arrange
        Customer customer = createTestCustomer("CUST1", 40, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST1")
                .policyTerm(15)
                .sumAssured(500000L)
                .annualPremium(10000L)
                .paymentFrequency("MONTHLY")
                .nomineeName("Jane Doe")
                .build();

        // Act
        Proposal response = proposalService.createProposal(request);

        // Assert
        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        assertEquals(500000L, response.getSumAssured());
    }

    @Test
    void testPolicyTermValidation() {
        // Arrange
        Customer customer = createTestCustomer("CUST2", 35, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST2")
                .policyTerm(12) // Invalid term
                .sumAssured(500000L)
                .annualPremium(10000L)
                .paymentFrequency("MONTHLY")
                .nomineeName("Jane Doe")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.createProposal(request));
    }

    @Test
    void testSumAssuredMinimumValidation() {
        // Arrange
        Customer customer = createTestCustomer("CUST3", 30, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST3")
                .policyTerm(10)
                .sumAssured(50000L) // Below minimum
                .annualPremium(5000L)
                .paymentFrequency("ANNUAL")
                .nomineeName("Jane Doe")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.createProposal(request));
    }

    @Test
    void testSumAssuredMaximumValidation() {
        // Arrange
        Customer customer = createTestCustomer("CUST4", 25, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST4")
                .policyTerm(20)
                .sumAssured(60000000L) // Above maximum
                .annualPremium(100000L)
                .paymentFrequency("ANNUAL")
                .nomineeName("Jane Doe")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.createProposal(request));
    }

    @Test
    void testAnnualPremiumMinimumValidation() {
        // Arrange
        Customer customer = createTestCustomer("CUST5", 28, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST5")
                .policyTerm(15)
                .sumAssured(500000L)
                .annualPremium(2000L) // Below minimum
                .paymentFrequency("ANNUAL")
                .nomineeName("Jane Doe")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.createProposal(request));
    }

    @Test
    void testPANValidationForHighPremium() {
        // Arrange
        Customer customer = createTestCustomer("CUST6", 32, null); // No PAN
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST6")
                .policyTerm(20)
                .sumAssured(1000000L)
                .annualPremium(60000L) // Above 50000 threshold
                .paymentFrequency("ANNUAL")
                .nomineeName("Jane Doe")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.createProposal(request));
    }

    @Test
    void testPaymentFrequencyValidation() {
        // Arrange
        Customer customer = createTestCustomer("CUST7", 38, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST7")
                .policyTerm(25)
                .sumAssured(800000L)
                .annualPremium(20000L)
                .paymentFrequency("INVALID") // Invalid frequency
                .nomineeName("Jane Doe")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.createProposal(request));
    }

    @Test
    void testNomineeCannotBeSameAsCustomer() {
        // Arrange
        Customer customer = createTestCustomer("CUST8", 45, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST8")
                .policyTerm(30)
                .sumAssured(2000000L)
                .annualPremium(50000L)
                .paymentFrequency("ANNUAL")
                .nomineeName("John Doe") // Same as customer
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.createProposal(request));
    }

    @Test
    void testProposalSubmissionSuccess() {
        // Arrange
        Customer customer = createTestCustomer("CUST9", 50, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST9")
                .policyTerm(10)
                .sumAssured(1500000L)
                .annualPremium(30000L)
                .paymentFrequency("HALF_YEARLY")
                .nomineeName("Jane Doe")
                .build();

        Proposal proposal = proposalService.createProposal(request);

        // Act
        Proposal submittedProposal = proposalService.submitProposal(proposal.getProposalId());

        // Assert
        assertEquals("SUBMITTED", submittedProposal.getStatus());
        assertNotNull(submittedProposal.getPolicyNumber());
    }

    @Test
    void testProposalCannotBeSubmittedTwice() {
        // Arrange
        Customer customer = createTestCustomer("CUST10", 33, "ABCDE1234F");
        ProposalRequest request = ProposalRequest.builder()
                .customerId("CUST10")
                .policyTerm(15)
                .sumAssured(750000L)
                .annualPremium(15000L)
                .paymentFrequency("QUARTERLY")
                .nomineeName("Jane Doe")
                .build();

        Proposal proposal = proposalService.createProposal(request);
        proposalService.submitProposal(proposal.getProposalId());

        // Act & Assert
        assertThrows(BusinessException.class, () -> proposalService.submitProposal(proposal.getProposalId()));
    }
}
