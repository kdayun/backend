package com.example.wonway.session.exception;

import java.util.UUID;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(UUID sessionId) {
        super("상담 세션을 찾을 수 없습니다: " + sessionId);
    }
}
