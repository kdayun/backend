package com.example.wonway.policy.service;

import com.example.wonway.common.contract.ChannelRankingCondition;
import com.example.wonway.common.contract.ChannelRecommendation;
import com.example.wonway.common.contract.ChannelType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ChannelRankingServiceImpl implements ChannelRankingService {

    private static final String OFFICIAL_WOORI_URL = "https://www.wooribank.com";

    @Override
    public List<ChannelRecommendation> rankChannels(ChannelRankingCondition condition) {
        if (condition == null || condition.policies().isEmpty()) {
            return List.of();
        }

        boolean identityAvailable = Boolean.TRUE.equals(condition.identityVerificationAvailable());
        List<ChannelRecommendation> recommendations = new ArrayList<>();

        recommendations.add(recommend(
                ChannelType.MOBILE_APP,
                identityAvailable,
                identityAvailable ? 90 : 0,
                identityAvailable ? "앱에서 본인확인 후 비대면 처리가 가능합니다." : "본인확인 수단이 없어 앱 처리가 어렵습니다."
        ));
        recommendations.add(recommend(
                ChannelType.WEBSITE,
                identityAvailable,
                identityAvailable ? 80 : 0,
                identityAvailable ? "웹에서 본인확인 후 처리를 시도할 수 있습니다." : "본인확인 수단이 없어 웹 처리가 어렵습니다."
        ));
        recommendations.add(recommend(
                ChannelType.PHONE,
                true,
                70,
                "전화 상담을 통해 필요한 절차를 안내받을 수 있습니다."
        ));
        recommendations.add(recommend(
                ChannelType.BRANCH,
                true,
                60,
                "대면 확인이 필요하면 영업점에서 안내받을 수 있습니다."
        ));
        recommendations.add(recommend(
                ChannelType.CHATBOT,
                false,
                0,
                "챗봇은 검증된 정책 안내만 제공하며 실제 업무를 처리하지 않습니다."
        ));

        return recommendations.stream()
                .sorted(Comparator.comparingInt(ChannelRecommendation::score).reversed())
                .toList();
    }

    private ChannelRecommendation recommend(
            ChannelType channel,
            boolean processable,
            int score,
            String reason
    ) {
        return new ChannelRecommendation(channel, score, processable, List.of(reason), officialUrl(channel));
    }

    private String officialUrl(ChannelType channel) {
        return OFFICIAL_WOORI_URL;
    }
}
