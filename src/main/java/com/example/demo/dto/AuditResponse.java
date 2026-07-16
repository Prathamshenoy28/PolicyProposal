package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditResponse {
    private String auditId;
    private String entityType;
    private String entityId;
    private String action;
    private String details;
    private Long timestamp;
}
