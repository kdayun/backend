package com.example.wonway.api.dto.response;

import java.util.List;
import java.util.UUID;

public record ConsultationMessageResponse(
        UUID sessionId,
        String answer,
        List<ChannelRecommendationResponse> recommendations,
        String consultationSummary
) {
    public ConsultationMessageResponse {
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }
}
