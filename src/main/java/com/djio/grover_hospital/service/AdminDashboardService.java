package com.djio.grover_hospital.service;

import com.djio.grover_hospital.model.dto.response.DashboardStatsResponse;
import com.djio.grover_hospital.model.enums.BookingStatus;
import com.djio.grover_hospital.model.enums.ProfileUpdateStatus;
import com.djio.grover_hospital.repository.BlogPostRepository;
import com.djio.grover_hospital.repository.BookingRepository;
import com.djio.grover_hospital.repository.FeedbackRepository;
import com.djio.grover_hospital.repository.ProfileUpdateRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;
    private final BlogPostRepository blogPostRepository;
    private final ProfileUpdateRequestRepository profileUpdateRequestRepository;

    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .unreadFeedback(feedbackRepository.countByIsRead(false))
                .pendingAppointments(bookingRepository.countByStatus(BookingStatus.PENDING))
                .profileUpdatesPending(profileUpdateRequestRepository.countByStatus(ProfileUpdateStatus.PENDING))
                .articleDrafts(blogPostRepository.countByIsPublishedFalse())
                .build();
    }
}