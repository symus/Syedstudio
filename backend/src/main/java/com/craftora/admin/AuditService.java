package com.craftora.admin;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UUID actorId, String action, String targetType, UUID targetId, String details) {
        repository.save(new AuditLog(actorId, action, targetType, targetId, details));
    }
}
