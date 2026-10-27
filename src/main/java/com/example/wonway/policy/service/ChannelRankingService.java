package com.example.wonway.policy.service;

import com.example.wonway.common.contract.ChannelRankingCondition;
import com.example.wonway.common.contract.ChannelRecommendation;

import java.util.List;

/**
 * 담당 B가 구현하는 상담 채널 처리 가능 여부 및 순위 계산 계약입니다.
 */
public interface ChannelRankingService {

    List<ChannelRecommendation> rankChannels(ChannelRankingCondition condition);
}
