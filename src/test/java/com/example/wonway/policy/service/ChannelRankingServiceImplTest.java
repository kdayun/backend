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
                "AUTO_TRANSFER_DATE_CHANGE",
                "INDIVIDUAL",
                "ACCOUNT_AUTO_TRANSFER",
                false,
                "AUTOMATIC",
                20,
                26,
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
                "AUTO_TRANSFER_DATE_CHANGE",
                "INDIVIDUAL",
                "ACCOUNT_AUTO_TRANSFER",
                true,
                "AUTOMATIC",
                20,
                26,
                List.of()
        );

        assertThat(service.rankChannels(condition)).isEmpty();
    }

    @Test
    void ranksAppWebsiteAndPhoneAsTopThreeForScenario() {
        ChannelRankingCondition condition = new ChannelRankingCondition(
                "AUTO_TRANSFER_DATE_CHANGE",
                "INDIVIDUAL",
                "ACCOUNT_AUTO_TRANSFER",
                true,
                "AUTOMATIC",
                20,
                26,
                List.of(policy())
        );

        assertThat(service.rankChannels(condition).stream()
                .filter(ChannelRecommendation::processable)
                .limit(3)
                .map(ChannelRecommendation::channel))
                .containsExactly(ChannelType.MOBILE_APP, ChannelType.WEBSITE, ChannelType.PHONE);
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
