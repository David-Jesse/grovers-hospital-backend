package com.djio.grover_hospital.service;


import com.djio.grover_hospital.exception.BadRequestException;
import com.djio.grover_hospital.exception.ResourceNotFoundException;
import com.djio.grover_hospital.model.dto.request.PromotionRequest;
import com.djio.grover_hospital.model.dto.response.PromotionResponse;
import com.djio.grover_hospital.model.entity.Promotion;
import com.djio.grover_hospital.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;

    // ==== Public read ===
    public List<PromotionResponse> getCurrentlyActive() {
        return promotionRepository.findCurrentlyActive(OffsetDateTime.now())
                .stream()
                .map(PromotionResponse::from)
                .toList();
    }

    // === Admin ===

    public List<PromotionResponse> getAllForAdmin() {
        return promotionRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(PromotionResponse::from)
                .toList();
    }

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        validateDateWindow(request.getStartsAt(), request.getEndsAt());

        Promotion promotion = Promotion.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        return PromotionResponse.from(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse update(Long id, PromotionRequest request) {
        validateDateWindow(request.getStartsAt(), request.getEndsAt());

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        promotion.setTitle(request.getTitle());
        promotion.setSubtitle(request.getSubtitle());
        promotion.setDescription(request.getDescription());
        promotion.setImageUrl(request.getImageUrl());
        promotion.setLinkUrl(request.getLinkUrl());
        promotion.setStartsAt(request.getStartsAt());
        promotion.setEndsAt(request.getEndsAt());
        if (request.getDisplayOrder() != null) promotion.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) promotion.setIsActive(request.getIsActive());

        return PromotionResponse.from(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse toggleActive(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        promotion.setIsActive(!Boolean.TRUE.equals(promotion.getIsActive()));
        return PromotionResponse.from(promotionRepository.save(promotion));
    }

    @Transactional
    public void delete(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion", "id", id);
        }
        promotionRepository.deleteById(id);
    }

    private void validateDateWindow(OffsetDateTime startsAt, OffsetDateTime endsAt) {
        if (startsAt != null && endsAt != null && startsAt.isAfter(endsAt)) {
            throw new BadRequestException("Start date must be before end date");
        }
    }


}