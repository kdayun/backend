package com.example.wonway.policy.mapper;

import com.example.wonway.common.contract.PolicyResult;
import com.example.wonway.policy.domain.ConsultationPolicy;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {

    public PolicyResult toResult(ConsultationPolicy policy) {
        return new PolicyResult(
                policy.getId(),
                policy.getPolicyCode(),
                policy.getTitle(),
                policy.getVerifiedContent(),
                policy.getEffectiveFrom(),
                policy.getEffectiveTo()
        );
    }
}
