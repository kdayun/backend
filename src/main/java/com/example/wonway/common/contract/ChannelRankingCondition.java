package com.example.wonway.common.contract;

import java.util.List;

public record ChannelRankingCondition(
        String intent,
        String customerType,
        String productType,
        Boolean identityVerificationAvailable,
        String transferMethod,
        Integer currentTransferDay,
        Integer requestedTransferDay,
        List<PolicyResult> policies
) {
    public ChannelRankingCondition {
        policies = policies == null ? List.of() : List.copyOf(policies);
    }
}
