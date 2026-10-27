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
    private static final String AUTO_TRANSFER_WEB_URL =
            "https://spib.wooribank.com/pib/Dream?basePage=PSTRS0018&nextPage=ps&withyou=CMCOM0001";
    private static final String SERVICE_HOURS_URL =
            "https://spot.wooribank.com/pot/Dream?withyou=CQIBG0024";

    @Override
    public List<ChannelRecommendation> rankChannels(ChannelRankingCondition condition) {
        if (condition == null || condition.policies().isEmpty()) {
            return List.of();
        }
        if (!isAutoTransferDateChange(condition)) {
            return List.of();
        }

        boolean identityAvailable = Boolean.TRUE.equals(condition.identityVerificationAvailable());
        List<ChannelRecommendation> recommendations = new ArrayList<>();

        recommendations.add(recommend(
                ChannelType.MOBILE_APP,
                identityAvailable,
                identityAvailable ? 100 : 0,
                identityAvailable
                        ? "스마트뱅킹에서 본인확인 후 자동이체 변경을 처리할 수 있어 이동 단계가 가장 적습니다."
                        : "본인확인 수단이 없어 스마트뱅킹 처리가 어렵습니다.",
                OFFICIAL_WOORI_URL
        ));
        recommendations.add(recommend(
                ChannelType.WEBSITE,
                identityAvailable,
                identityAvailable ? 85 : 0,
                identityAvailable
                        ? "개인 인터넷뱅킹의 자동이체조회/등록/변경 메뉴를 이용할 수 있습니다. 변경 이용시간은 06:00~23:59이며 휴일에는 이용할 수 없습니다."
                        : "본인확인 수단이 없어 인터넷뱅킹 처리가 어렵습니다.",
                AUTO_TRANSFER_WEB_URL
        ));
        recommendations.add(recommend(
                ChannelType.PHONE,
                true,
                65,
                "텔레뱅킹 신청이 가능하며 고객센터 전화상담은 평일 09:00~18:00에 이용할 수 있습니다.",
                SERVICE_HOURS_URL
        ));
        recommendations.add(recommend(
                ChannelType.BRANCH,
                true,
                50,
                "비대면 본인확인이 어렵거나 자동이체 종류가 온라인 변경 대상이 아니면 영업점에서 서면으로 신청할 수 있습니다.",
                OFFICIAL_WOORI_URL
        ));
        recommendations.add(recommend(
                ChannelType.CHATBOT,
                false,
                0,
                "챗봇은 변경 조건과 경로만 안내하며 자동이체를 직접 변경하지 않습니다.",
                OFFICIAL_WOORI_URL
        ));

        return recommendations.stream()
                .sorted(Comparator.comparingInt(ChannelRecommendation::score).reversed())
                .toList();
    }

    private ChannelRecommendation recommend(
            ChannelType channel,
            boolean processable,
            int score,
            String reason,
            String officialUrl
    ) {
        String deadlineReason = "변경은 기존 이체지정일 전 영업일까지 신청해야 합니다.";
        return new ChannelRecommendation(
                channel,
                score,
                processable,
                List.of(reason, deadlineReason),
                officialUrl
        );
    }

    private boolean isAutoTransferDateChange(ChannelRankingCondition condition) {
        return "AUTO_TRANSFER_DATE_CHANGE".equalsIgnoreCase(condition.intent())
                && "ACCOUNT_AUTO_TRANSFER".equalsIgnoreCase(condition.productType())
                && "AUTOMATIC".equalsIgnoreCase(condition.transferMethod())
                && isValidDay(condition.currentTransferDay())
                && isValidDay(condition.requestedTransferDay())
                && !condition.currentTransferDay().equals(condition.requestedTransferDay());
    }

    private boolean isValidDay(Integer day) {
        return day != null && day >= 1 && day <= 31;
    }
}
