package com.example.wonway.policy.service;

import com.example.wonway.common.contract.PolicyResult;
import com.example.wonway.common.contract.PolicySearchCondition;
import com.example.wonway.policy.domain.ConsultationPolicy;
import com.example.wonway.policy.domain.VerificationStatus;
import com.example.wonway.policy.mapper.PolicyMapper;
import com.example.wonway.policy.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyServiceImplTest {

    private PolicyRepository policyRepository;
    private PolicyServiceImpl policyService;

    @BeforeEach
    void setUp() {
        policyRepository = mock(PolicyRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-10-27T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        policyService = new PolicyServiceImpl(policyRepository, new PolicyMapper(), clock);
    }

    @Test
    void returnsOnlyPoliciesEffectiveToday() {
        PolicySearchCondition condition = new PolicySearchCondition("대출", "개인", "전세대출", true);
        ConsultationPolicy active = policy("ACTIVE", LocalDate.of(2026, 1, 1), null);
        ConsultationPolicy expired = policy(
                "EXPIRED",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );
        when(policyRepository
                .findByIntentIgnoreCaseAndCustomerTypeIgnoreCaseAndProductTypeIgnoreCaseAndRequiresIdentityVerificationAndVerificationStatus(
                        "대출", "개인", "전세대출", true, VerificationStatus.VERIFIED
                ))
                .thenReturn(List.of(active, expired));

        List<PolicyResult> result = policyService.findApplicablePolicies(condition);

        assertThat(result).extracting(PolicyResult::policyCode).containsExactly("ACTIVE");
    }

    @Test
    void returnsEmptyListWhenRepositoryHasNoPolicy() {
        PolicySearchCondition condition = new PolicySearchCondition("카드", "개인", "신용카드", false);
        when(policyRepository
                .findByIntentIgnoreCaseAndCustomerTypeIgnoreCaseAndProductTypeIgnoreCaseAndRequiresIdentityVerificationAndVerificationStatus(
                        "카드", "개인", "신용카드", false, VerificationStatus.VERIFIED
                ))
                .thenReturn(List.of());

        assertThat(policyService.findApplicablePolicies(condition)).isEmpty();
    }

    private ConsultationPolicy policy(String code, LocalDate from, LocalDate to) {
        return new ConsultationPolicy(
                code,
                "테스트 정책",
                "대출",
                "개인",
                "전세대출",
                true,
                "검증된 테스트 안내",
                "https://www.wooribank.com",
                "테스트 출처",
                "https://www.wooribank.com",
                from,
                to,
                LocalDate.of(2026, 10, 1),
                "테스트 검증자",
                VerificationStatus.VERIFIED
        );
    }
}
