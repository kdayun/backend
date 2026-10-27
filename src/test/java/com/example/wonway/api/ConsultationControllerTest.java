package com.example.wonway.api;

import com.example.wonway.agent.AgentService;
import com.example.wonway.agent.model.AgentResult;
import com.example.wonway.common.contract.ChannelRecommendation;
import com.example.wonway.common.contract.ChannelType;
import com.example.wonway.session.ConsultationSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConsultationControllerTest {

    private ConsultationSessionService sessionService;
    private AgentService agentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        sessionService = mock(ConsultationSessionService.class);
        agentService = mock(AgentService.class);
        ConsultationController controller = new ConsultationController(sessionService, agentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsConsultationSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(sessionService.createSession()).thenReturn(sessionId);

        mockMvc.perform(post("/api/v1/consultations"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()));
    }

    @Test
    void validatesSessionAndReturnsAgentResult() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(agentService.chat(sessionId, "전세대출 상담이 필요해요")).thenReturn(new AgentResult(
                "전화 상담을 권장합니다.",
                List.of(new ChannelRecommendation(
                        ChannelType.PHONE,
                        70,
                        true,
                        List.of("전화 안내 가능"),
                        "https://www.wooribank.com"
                )),
                "전세대출 상담 요청"
        ));

        mockMvc.perform(post("/api/v1/consultations/{sessionId}/messages", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"전세대출 상담이 필요해요\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("전화 상담을 권장합니다."))
                .andExpect(jsonPath("$.recommendations[0].channel").value("PHONE"));

        verify(sessionService).validateActive(sessionId);
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/consultations/{sessionId}/messages", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void closesConsultationSession() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/consultations/{sessionId}", sessionId))
                .andExpect(status().isNoContent());

        verify(sessionService).closeSession(sessionId);
    }
}
