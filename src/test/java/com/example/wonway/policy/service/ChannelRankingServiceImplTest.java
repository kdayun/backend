package com.example.wonway.policy.service;

import com.example.wonway.common.contract.ChannelRankingCondition;
import com.example.wonway.common.contract.ChannelRecommendation;
import com.example.wonway.common.contract.ChannelType;
import com.example.wonway.common.contract.PolicyResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelRankingServiceImplTest {

    private final ChannelRankingService service = new ChannelRankingServiceImpl();

    @Test
    void excludesDigitalProcessingWhenIdentityVerificationIsUnavailable() {
        ChannelRankingCondition condition = new ChannelRankingCondition(
                "대출",
                "개인",
                "전세대출",
                false,
                List.of(policy())
        );

        List<ChannelRecommendation> result = service.rankChannels(condition);

        assertThat(result.stream()
                .filter(ChannelRecommendation::processable)
                .map(ChannelRecommendation::channel))
                .containsExactly(ChannelType.PHONE, ChannelType.BRANCH);
        assertThat(result.stream()
                .filter(recommendation -> recommendation.channel() == ChannelType.MOBILE_APP)
                .findFirst()
                .orElseThrow()
                .processable()).isFalse();
    }

    @Test
    void returnsEmptyListWithoutVerifiedPolicies() {
        ChannelRankingCondition condition = new ChannelRankingCondition(
                "대출", "개인", "전세대출", true, List.of()
        );

        assertThat(service.rankChannels(condition)).isEmpty();
    }

    private PolicyResult policy() {
        return new PolicyResult(
                1L,
                "POLICY-1",
                "테스트 정책",
                "검증된 안내",
                LocalDate.of(2026, 1, 1),
                null
        );
    }
}
