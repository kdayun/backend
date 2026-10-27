package com.example.wonway.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultationMessageRequest(
        @NotBlank(message = "상담 메시지는 필수입니다.")
        @Size(max = 1000, message = "상담 메시지는 1000자를 초과할 수 없습니다.")
        String message
) {
}
