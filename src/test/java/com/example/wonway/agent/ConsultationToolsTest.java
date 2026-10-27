package com.example.wonway.agent;

import com.example.wonway.common.contract.ChannelRankingCondition;
import com.example.wonway.common.contract.ChannelRecommendation;
import com.example.wonway.common.contract.ChannelType;
import com.example.wonway.common.contract.PolicyResult;
import com.example.wonway.common.contract.PolicySearchCondition;
import com.example.wonway.policy.service.ChannelRankingService;
import com.example.wonway.policy.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ToolContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultationToolsTest {

    private PolicyService policyService;
    private ChannelRankingService rankingService;
    private ConsultationTools tools;

    @BeforeEach
    void setUp() {
        policyService = mock(PolicyService.class);
        rankingService = mock(ChannelRankingService.class);
        tools = new ConsultationTools(policyService, rankingService);
    }

    @Test
    void rankingUsesServerVerifiedPoliciesInsteadOfModelSuppliedPolicies() {
        UUID sessionId = UUID.randomUUID();
        ToolContext context = new ToolContext(Map.of(
                ConsultationTools.SESSION_ID_CONTEXT_KEY,
                sessionId.toString()
        ));
        PolicyResult verified = policy("VERIFIED");
        PolicyResult modelSupplied = policy("MODEL-SUPPLIED");
        when(policyService.findApplicablePolicies(any())).thenReturn(List.of(verified));
        when(rankingService.rankChannels(any())).thenReturn(List.of(
                new ChannelRecommendation(
                        ChannelType.PHONE,
                        70,
                        true,
                        List.of("전화 안내 가능"),
                        "https://www.wooribank.com"
                )
        ));

        tools.findApplicablePolicies(
                new PolicySearchCondition(
                        "AUTO_TRANSFER_DATE_CHANGE",
                        "INDIVIDUAL",
                        "ACCOUNT_AUTO_TRANSFER",
                        true,
                        "AUTOMATIC",
                        20,
                        26
                ),
                context
        );
        tools.rankConsultationChannels(
                new ChannelRankingCondition(
                        "MODEL_VALUE",
                        "MODEL_VALUE",
                        "MODEL_VALUE",
                        true,
                        "MANUAL",
                        1,
                        2,
                        List.of(modelSupplied)
                ),
                context
        );

        ArgumentCaptor<ChannelRankingCondition> captor = ArgumentCaptor.forClass(ChannelRankingCondition.class);
        verify(rankingService).rankChannels(captor.capture());
        assertThat(captor.getValue().policies()).containsExactly(verified);
        assertThat(captor.getValue().intent()).isEqualTo("AUTO_TRANSFER_DATE_CHANGE");
        assertThat(captor.getValue().transferMethod()).isEqualTo("AUTOMATIC");
        assertThat(captor.getValue().currentTransferDay()).isEqualTo(20);
        assertThat(captor.getValue().requestedTransferDay()).isEqualTo(26);
        assertThat(tools.consumeRecommendations(sessionId))
                .extracting(ChannelRecommendation::channel)
                .containsExactly(ChannelType.PHONE);
    }

    private PolicyResult policy(String code) {
        return new PolicyResult(
                1L,
                code,
                "테스트 정책",
                "검증된 안내",
                LocalDate.of(2026, 1, 1),
                null
        );
    }
}
