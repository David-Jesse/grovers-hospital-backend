package com.djio.grover_hospital.service;

import com.djio.grover_hospital.model.entity.AccessLog;
import com.djio.grover_hospital.repository.AccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AccessLogRepository accessLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String userType, String action,
                    String resourceType, Long resourceId, HttpServletRequest request) {
        AccessLog log = AccessLog.builder()
                .userId(userId)
                .userType(userType)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(getClientIp(request))
                .build();
        accessLogRepository.save(log);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String userType, String action, HttpServletRequest request) {
        log(userId, userType, action, null, null, request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}