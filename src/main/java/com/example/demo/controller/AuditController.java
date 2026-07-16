package com.example.demo.controller;

import com.example.demo.model.Audit;
import com.example.demo.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audits")
public class AuditController {
    @Autowired
    private AuditService auditService;

    @GetMapping
    public ResponseEntity<List<Audit>> getAllAudits() {
        List<Audit> audits = auditService.getAllAudits();
        return ResponseEntity.ok(audits);
    }

    @GetMapping("/{entityId}")
    public ResponseEntity<List<Audit>> getAuditsByEntityId(@PathVariable String entityId) {
        List<Audit> audits = auditService.getAuditsByEntityId(entityId);
        return ResponseEntity.ok(audits);
    }
}
