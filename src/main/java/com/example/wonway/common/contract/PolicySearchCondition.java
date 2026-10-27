package com.example.wonway.common.contract;

public record PolicySearchCondition(
        String intent,
        String customerType,
        String productType,
        Boolean requiresIdentityVerification
) {
}
