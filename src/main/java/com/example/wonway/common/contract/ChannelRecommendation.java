package com.example.wonway.common.contract;

import java.util.List;

public record ChannelRecommendation(
        ChannelType channel,
        int score,
        boolean processable,
        List<String> reasons,
        String officialPageUrl
) {
    public ChannelRecommendation {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
