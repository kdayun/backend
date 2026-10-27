package com.example.wonway.agent;

import com.example.wonway.agent.model.AgentResult;

import java.util.UUID;

/**
 * 담당 A가 구현하는 AI 상담 진입점입니다.
 */
public interface AgentService {

    AgentResult chat(UUID sessionId, String message);
}
