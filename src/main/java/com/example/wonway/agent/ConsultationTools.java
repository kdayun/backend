package com.example.wonway.agent;

import com.example.wonway.common.contract.ChannelRankingCondition;
import com.example.wonway.common.contract.ChannelRecommendation;
import com.example.wonway.common.contract.PolicyResult;
import com.example.wonway.common.contract.PolicySearchCondition;
import com.example.wonway.policy.service.ChannelRankingService;
import com.example.wonway.policy.service.PolicyService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ConsultationTools {

    static final String SESSION_ID_CONTEXT_KEY = "sessionId";

    private final PolicyService policyService;
    private final ChannelRankingService channelRankingService;
    private final ConcurrentMap<String, List<PolicyResult>> verifiedPolicies = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<ChannelRecommendation>> verifiedRecommendations = new ConcurrentHashMap<>();

    public ConsultationTools(PolicyService policyService, ChannelRankingService channelRankingService) {
        this.policyService = policyService;
        this.channelRankingService = channelRankingService;
    }

    @Tool(
            name = "findApplicablePolicies",
            description = "사용자의 상담 의도, 고객 유형, 상품 유형과 본인확인 필요 여부에 맞는 검증된 은행 정책을 조회한다."
    )
    public List<PolicyResult> findApplicablePolicies(
            @ToolParam(description = "정책 검색 조건") PolicySearchCondition condition,
            ToolContext toolContext
    ) {
        String sessionId = sessionId(toolContext);
        List<PolicyResult> policies = List.copyOf(policyService.findApplicablePolicies(condition));
        verifiedPolicies.put(sessionId, policies);
        return policies;
    }

    @Tool(
            name = "rankConsultationChannels",
            description = "검증된 정책을 기준으로 실제 처리 가능한 상담 채널의 점수와 이유를 계산한다."
    )
    public List<ChannelRecommendation> rankConsultationChannels(
            @ToolParam(description = "채널 순위 계산 조건") ChannelRankingCondition condition,
            ToolContext toolContext
    ) {
        String sessionId = sessionId(toolContext);
        List<PolicyResult> policies = verifiedPolicies.getOrDefault(sessionId, List.of());
        if (policies.isEmpty()) {
            verifiedRecommendations.put(sessionId, List.of());
            return List.of();
        }

        ChannelRankingCondition verifiedCondition = new ChannelRankingCondition(
                condition.intent(),
                condition.customerType(),
                condition.productType(),
                condition.identityVerificationAvailable(),
                policies
        );
        List<ChannelRecommendation> recommendations = List.copyOf(
                channelRankingService.rankChannels(verifiedCondition)
        );
        verifiedRecommendations.put(sessionId, recommendations);
        return recommendations;
    }

    public List<ChannelRecommendation> consumeRecommendations(UUID sessionId) {
        return verifiedRecommendations.getOrDefault(sessionId.toString(), List.of())
                .stream()
                .filter(ChannelRecommendation::processable)
                .toList();
    }

    public void clearInvocationState(UUID sessionId) {
        String key = sessionId.toString();
        verifiedPolicies.remove(key);
        verifiedRecommendations.remove(key);
    }

    private String sessionId(ToolContext toolContext) {
        Object value = toolContext.getContext().get(SESSION_ID_CONTEXT_KEY);
        if (!(value instanceof String sessionId) || sessionId.isBlank()) {
            throw new IllegalArgumentException("도구 호출에 상담 세션 ID가 없습니다.");
        }
        return sessionId;
    }
}
