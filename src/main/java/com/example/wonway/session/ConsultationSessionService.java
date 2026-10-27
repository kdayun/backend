package com.example.wonway.session;

import java.util.UUID;

/**
 * 담당 C가 구현하는 상담 세션 수명주기 계약입니다.
 */
public interface ConsultationSessionService {

    UUID createSession();

    void validateActive(UUID sessionId);

    SessionStatus getStatus(UUID sessionId);

    void closeSession(UUID sessionId);
}
