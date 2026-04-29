package com.djio.grover_hospital.service;


import com.djio.grover_hospital.model.dto.response.PromotionResponse;
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

    public List<PromotionResponse> getCurrentlyActive() {
        return promotionRepository.findCurrentlyActive(OffsetDateTime.now())
                .stream()
                .map(PromotionResponse::from)
                .toList();
    }
}