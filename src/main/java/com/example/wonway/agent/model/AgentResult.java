package com.example.wonway.agent.model;

import com.example.wonway.common.contract.ChannelRecommendation;

import java.util.List;

public record AgentResult(
        String answer,
        List<ChannelRecommendation> recommendations,
        String consultationSummary
) {
    public AgentResult {
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }
}
