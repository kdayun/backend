package com.example.wonway.api;

import com.example.wonway.agent.AgentService;
import com.example.wonway.agent.model.AgentResult;
import com.example.wonway.api.dto.request.ConsultationMessageRequest;
import com.example.wonway.api.dto.response.ChannelRecommendationResponse;
import com.example.wonway.api.dto.response.ConsultationMessageResponse;
import com.example.wonway.api.dto.response.ConsultationSessionResponse;
import com.example.wonway.common.contract.ChannelRecommendation;
import com.example.wonway.session.ConsultationSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consultations")
public class ConsultationController {

    private final ConsultationSessionService sessionService;
    private final AgentService agentService;

    public ConsultationController(ConsultationSessionService sessionService, AgentService agentService) {
        this.sessionService = sessionService;
        this.agentService = agentService;
    }

    @PostMapping
    public ResponseEntity<ConsultationSessionResponse> createSession() {
        UUID sessionId = sessionService.createSession();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ConsultationSessionResponse(sessionId));
    }

    @PostMapping("/{sessionId}/messages")
    public ConsultationMessageResponse sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ConsultationMessageRequest request
    ) {
        sessionService.validateActive(sessionId);
        AgentResult result = agentService.chat(sessionId, request.message().trim());

        return new ConsultationMessageResponse(
                sessionId,
                result.answer(),
                result.recommendations().stream().map(this::toResponse).toList(),
                result.consultationSummary()
        );
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> closeSession(@PathVariable UUID sessionId) {
        sessionService.closeSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    private ChannelRecommendationResponse toResponse(ChannelRecommendation recommendation) {
        return new ChannelRecommendationResponse(
                recommendation.channel().name(),
                recommendation.score(),
                recommendation.reasons(),
                recommendation.officialPageUrl()
        );
    }
}
