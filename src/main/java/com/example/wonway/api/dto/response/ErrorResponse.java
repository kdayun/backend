package com.example.wonway.api.dto.response;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        String path
) {
}
