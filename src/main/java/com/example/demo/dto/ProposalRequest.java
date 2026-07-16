package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalRequest {
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotNull(message = "Policy term is required")
    private Integer policyTerm;
    
    @NotNull(message = "Sum assured is required")
    @Min(value = 100000, message = "Sum assured must be at least Rs. 1,00,000")
    @Max(value = 50000000L, message = "Sum assured must not exceed Rs. 5,00,00,000")
    private Long sumAssured;
    
    @NotNull(message = "Annual premium is required")
    @Min(value = 5000, message = "Annual premium must be at least Rs. 5,000")
    private Long annualPremium;
    
    @NotBlank(message = "Payment frequency is required")
    private String paymentFrequency;
    
    @NotBlank(message = "Nominee name is required")
    private String nomineeName;
}
