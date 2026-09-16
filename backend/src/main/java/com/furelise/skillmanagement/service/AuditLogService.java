package com.furelise.skillmanagement.service;

import com.furelise.skillmanagement.model.AuditLog;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(User user, String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.furelise.skillmanagement.dto.AuditLogDto.AuditLogResponse> getAuditLogs(
            String entityType, String action, Long userId,
            java.time.ZonedDateTime startDate, java.time.ZonedDateTime endDate,
            org.springframework.data.domain.Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<AuditLog> spec = org.springframework.data.jpa.domain.Specification.where(null);

        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        return auditLogRepository.findAll(spec, pageable)
                .map(com.furelise.skillmanagement.dto.AuditLogDto.AuditLogResponse::from);
    }
}
