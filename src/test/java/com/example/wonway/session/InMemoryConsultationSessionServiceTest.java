package com.example.wonway.session;

import com.example.wonway.session.exception.SessionClosedException;
import com.example.wonway.session.exception.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InMemoryConsultationSessionServiceTest {

    private ChatMemory chatMemory;
    private InMemoryConsultationSessionService sessionService;

    @BeforeEach
    void setUp() {
        chatMemory = mock(ChatMemory.class);
        sessionService = new InMemoryConsultationSessionService(chatMemory);
    }

    @Test
    void createsAndClosesSessionAndClearsMemory() {
        UUID sessionId = sessionService.createSession();

        assertThat(sessionService.getStatus(sessionId)).isEqualTo(SessionStatus.ACTIVE);

        sessionService.closeSession(sessionId);

        assertThat(sessionService.getStatus(sessionId)).isEqualTo(SessionStatus.CLOSED);
        verify(chatMemory).clear(sessionId.toString());
        assertThatThrownBy(() -> sessionService.validateActive(sessionId))
                .isInstanceOf(SessionClosedException.class);
    }

    @Test
    void rejectsUnknownSession() {
        assertThatThrownBy(() -> sessionService.validateActive(UUID.randomUUID()))
                .isInstanceOf(SessionNotFoundException.class);
    }
}
