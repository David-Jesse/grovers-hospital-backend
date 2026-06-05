package com.djio.grover_hospital.service;

import com.djio.grover_hospital.model.entity.AccessLog;
import com.djio.grover_hospital.repository.AccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AccessLogRepository accessLogRepository;

    /**
     * Lazy factory for the AOP-proxied self. Using ObjectProvider instead of a direct
     * self-reference avoids the circular-dependency error at startup: the provider is
     * injected immediately, but the actual bean is fetched only when .getObject() is
     * called at request-handling time. Calling selfProvider.getObject().logAsync(...)
     * goes through the proxy, so @Async fires correctly.
     */
    private final ObjectProvider<AuditService> selfProvider;

    /**
     * Public entry point. SYNCHRONOUS — reads request headers while the request is
     * still attached to the live HTTP exchange, then hands off plain primitives to
     * the async save. Prevents the "request facade recycled" IllegalStateException.
     */
    public void log(Long userId, String userType, String action,
                    String resourceType, Long resourceId, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        selfProvider.getObject().logAsync(userId, userType, action, resourceType, resourceId, ipAddress);
    }

    public void log(Long userId, String userType, String action, HttpServletRequest request) {
        log(userId, userType, action, null, null, request);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(Long userId, String userType, String action,
                         String resourceType, Long resourceId, String ipAddress) {
        AccessLog log = AccessLog.builder()
                .userId(userId)
                .userType(userType)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .build();
        accessLogRepository.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}