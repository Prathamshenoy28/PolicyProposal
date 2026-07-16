package com.example.demo.service;

import com.example.demo.model.Audit;
import com.example.demo.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {
    private final AuditRepository auditRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public List<Audit> getAllAudits() {
        return auditRepository.findAll();
    }

    public List<Audit> getAuditsByEntityId(String entityId) {
        return auditRepository.findByEntityId(entityId);
    }
}
