package com.djio.grover_hospital.service;

import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.exception.UnauthorizedException;
import com.djio.grover_hospital.model.dto.response.PageResponse;
import com.djio.grover_hospital.model.dto.response.PortalNotificationResponse;
import com.djio.grover_hospital.model.dto.response.PortalNotificationSummary;
import com.djio.grover_hospital.model.entity.PortalNotification;
import com.djio.grover_hospital.model.enums.PortalNotificationType;
import com.djio.grover_hospital.model.enums.Role;
import com.djio.grover_hospital.repository.AdminRepository;
import com.djio.grover_hospital.repository.PortalNotificationRepository;
import com.djio.grover_hospital.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PortalNotificationService {

    private final PortalNotificationRepository notificationRepository;
    private final AdminRepository adminRepository;

    // === Creation (called from DefaultNotificationService) ===

    /**
     * Creates a notification for one specific user
     * REQUIRES_NEW so the notification persists even if the caller's transaction
     * is read-only or rolled back due to a downstream issue.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createForPatient(Long patientId, PortalNotificationType type, String message) {
        save(patientId, "PATIENT", type, message);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createForAdmin(Long adminId, PortalNotificationType type, String message) {
        save(adminId, "ADMIN", type, message);
    }

    /**
     * Sends a notification to every admin in the system
     * User for hospital-wide alerts like "new_booking_received"
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createForAllAdmins(PortalNotificationType type, String message) {
        adminRepository.findAll().forEach(admin -> save(admin.getId(), "ADMIN", type, message));
    }

    private void save(Long userId, String userType, PortalNotificationType type, String message) {
        PortalNotification notification = PortalNotification.builder()
                .userId(userId)
                .userType(userType)
                .type(type)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    // ==== Read operations for the current user (patient or admin) ====

    public PageResponse<PortalNotificationResponse> getMyNotifications(Pageable pageable, Boolean unreadOnly) {
        String userType = currentUserType();
        Long userId = SecurityUtils.getCurrentUserId();
        Page<PortalNotification> page = (unreadOnly != null && unreadOnly)
                ? notificationRepository.findByUserIdAndUserTypeIsReadOrderByCreatedAtDesc(
                userId, userType, false, pageable
        ) : notificationRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(
                userId, userType, pageable
        );
        return PageResponse.from(page, PortalNotificationResponse::from);
    }

    public PortalNotificationSummary getMySummary() {
        String userType = currentUserType();
        Long userId = SecurityUtils.getCurrentUserId();
        long unread = notificationRepository.countByUserIdAndUserTypeAndIsRead(userId, userType, false);
        long total = unread + notificationRepository.countByUserIdAndUserTypeAndIsRead(userId, userType, true);
        return PortalNotificationSummary.builder()
                .unreadCount(unread)
                .totalCount(total)
                .build();
    }

    // === Mutations ===

    @Transactional
    public PortalNotificationResponse markAsRead(Long id) {
        PortalNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        // Ownership check
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserType = currentUserType();
        if (!notification.getUserId().equals(currentUserId)
                || !notification.getUserType().equals(currentUserType)) {
            throw new UnauthorizedException("This notification does not belong to you!");
        }

        notification.setIsRead(true);
        return PortalNotificationResponse.from(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead() {
        return notificationRepository.markAllAsRead(SecurityUtils.getCurrentUserId(), currentUserType());
    }

    @Transactional
    public void delete(Long id) {
        PortalNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserType = currentUserType();
        if (!notification.getUserId().equals(currentUserId)
                || !notification.getUserType().equals(currentUserType)) {
            throw new UnauthorizedException("This notification does not belong to you");
        }

        notificationRepository.delete(notification);
    }

    private String currentUserType() {
        Role role = SecurityUtils.getCurrentUserRole();
        return role != null ? role.name() : "PATIENT";
    }
}