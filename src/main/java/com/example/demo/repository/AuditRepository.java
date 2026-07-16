package com.example.demo.repository;

import com.example.demo.model.Audit;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AuditRepository {
    private final Map<String, Audit> audits = new HashMap<>();

    public Audit save(Audit audit) {
        audits.put(audit.getAuditId(), audit);
        return audit;
    }

    public List<Audit> findAll() {
        return new ArrayList<>(audits.values());
    }

    public List<Audit> findByEntityId(String entityId) {
        return audits.values().stream()
                .filter(a -> a.getEntityId().equals(entityId))
                .toList();
    }
}
