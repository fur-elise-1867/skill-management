package com.furelise.skillmanagement.dto;

import com.furelise.skillmanagement.model.AuditLog;

import java.time.ZonedDateTime;

public class AuditLogDto {

    public record AuditLogResponse(
            Long id,
            Long userId,
            String userEmail,
            String userName,
            String action,
            String entityType,
            Long entityId,
            String details,
            ZonedDateTime createdAt
    ) {
        public static AuditLogResponse from(AuditLog log) {
            Long userId = log.getUser() != null ? log.getUser().getId() : null;
            String userEmail = log.getUser() != null ? log.getUser().getEmail() : null;
            String userName = log.getUser() != null
                    ? (log.getUser().getName() != null ? log.getUser().getName() : log.getUser().getEmail())
                    : null;

            return new AuditLogResponse(
                    log.getId(),
                    userId,
                    userEmail,
                    userName,
                    log.getAction(),
                    log.getEntityType(),
                    log.getEntityId(),
                    log.getDetails(),
                    log.getCreatedAt()
            );
        }
    }
}
