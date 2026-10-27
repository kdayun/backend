package com.example.wonway.session;

import com.example.wonway.session.exception.SessionClosedException;
import com.example.wonway.session.exception.SessionNotFoundException;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class InMemoryConsultationSessionService implements ConsultationSessionService {

    private final ConcurrentMap<UUID, SessionStatus> sessions = new ConcurrentHashMap<>();
    private final ChatMemory chatMemory;

    public InMemoryConsultationSessionService(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    @Override
    public UUID createSession() {
        UUID sessionId = UUID.randomUUID();
        sessions.put(sessionId, SessionStatus.ACTIVE);
        return sessionId;
    }

    @Override
    public void validateActive(UUID sessionId) {
        SessionStatus status = getStatus(sessionId);
        if (status == SessionStatus.CLOSED) {
            throw new SessionClosedException(sessionId);
        }
    }

    @Override
    public SessionStatus getStatus(UUID sessionId) {
        SessionStatus status = sessions.get(sessionId);
        if (status == null) {
            throw new SessionNotFoundException(sessionId);
        }
        return status;
    }

    @Override
    public void closeSession(UUID sessionId) {
        validateActive(sessionId);
        sessions.put(sessionId, SessionStatus.CLOSED);
        chatMemory.clear(sessionId.toString());
    }
}
