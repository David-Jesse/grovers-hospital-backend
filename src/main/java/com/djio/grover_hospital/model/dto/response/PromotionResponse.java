package com.djio.grover_hospital.model.dto.response;


import com.djio.grover_hospital.model.entity.Promotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Long id;
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private Integer displayOrder;

    public static PromotionResponse from(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .subtitle(promotion.getSubtitle())
                .description(promotion.getDescription())
                .imageUrl(promotion.getImageUrl())
                .linkUrl(promotion.getLinkUrl())
                .displayOrder(promotion.getDisplayOrder())
                .build();
    }
}
