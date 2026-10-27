package com.example.wonway.agent;

import com.example.wonway.agent.exception.AgentUnavailableException;
import com.example.wonway.agent.model.AgentAnswer;
import com.example.wonway.agent.model.AgentResult;
import com.example.wonway.common.contract.ChannelRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentServiceImpl implements AgentService {

    private final ChatClient chatClient;
    private final ConsultationTools consultationTools;

    public AgentServiceImpl(ChatClient chatClient, ConsultationTools consultationTools) {
        this.chatClient = chatClient;
        this.consultationTools = consultationTools;
    }

    @Override
    public AgentResult chat(UUID sessionId, String message) {
        if (sessionId == null || !StringUtils.hasText(message)) {
            throw new IllegalArgumentException("상담 세션과 메시지는 필수입니다.");
        }

        consultationTools.clearInvocationState(sessionId);
        try {
            AgentAnswer answer = chatClient.prompt()
                    .user(message)
                    .advisors(advisor -> advisor.param(
                            ChatMemory.CONVERSATION_ID,
                            sessionId.toString()
                    ))
                    .toolContext(Map.of(
                            ConsultationTools.SESSION_ID_CONTEXT_KEY,
                            sessionId.toString()
                    ))
                    .call()
                    .entity(AgentAnswer.class);

            if (answer == null || !StringUtils.hasText(answer.answer())) {
                throw new AgentUnavailableException("모델이 유효한 답변을 반환하지 않았습니다.");
            }

            List<ChannelRecommendation> recommendations = consultationTools.consumeRecommendations(sessionId);
            return new AgentResult(
                    answer.answer(),
                    recommendations,
                    StringUtils.hasText(answer.consultationSummary())
                            ? answer.consultationSummary()
                            : "상담 요약을 생성하지 못했습니다."
            );
        }
        catch (AgentUnavailableException exception) {
            throw exception;
        }
        catch (RuntimeException exception) {
            throw new AgentUnavailableException("AI 상담 모델 호출에 실패했습니다.", exception);
        }
        finally {
            consultationTools.clearInvocationState(sessionId);
        }
    }
}
