package com.example.wonway.session.exception;

import java.util.UUID;

public class SessionClosedException extends RuntimeException {

    public SessionClosedException(UUID sessionId) {
        super("이미 종료된 상담 세션입니다: " + sessionId);
    }
}
