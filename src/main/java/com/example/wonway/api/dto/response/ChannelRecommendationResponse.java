package com.example.wonway.api.dto.response;

import java.util.List;

public record ChannelRecommendationResponse(
        String channel,
        int score,
        List<String> reasons,
        String officialPageUrl
) {
    public ChannelRecommendationResponse {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
