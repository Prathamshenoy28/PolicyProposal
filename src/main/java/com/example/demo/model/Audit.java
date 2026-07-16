package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {
    private String auditId;
    private String entityType; // CUSTOMER, PROPOSAL
    private String entityId;
    private String action; // CREATE, UPDATE, SUBMIT
    private String details;
    private Long timestamp;
}
