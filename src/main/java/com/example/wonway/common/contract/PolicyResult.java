package com.example.wonway.common.contract;

import java.time.LocalDate;

public record PolicyResult(
        Long policyId,
        String policyCode,
        String title,
        String verifiedContent,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
