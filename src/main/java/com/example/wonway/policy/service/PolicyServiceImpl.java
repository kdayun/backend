package com.example.wonway.policy.service;

import com.example.wonway.common.contract.PolicyResult;
import com.example.wonway.common.contract.PolicySearchCondition;
import com.example.wonway.policy.domain.VerificationStatus;
import com.example.wonway.policy.mapper.PolicyMapper;
import com.example.wonway.policy.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
    private final Clock clock;

    @Autowired
    public PolicyServiceImpl(PolicyRepository policyRepository, PolicyMapper policyMapper) {
        this(policyRepository, policyMapper, Clock.systemDefaultZone());
    }

    PolicyServiceImpl(PolicyRepository policyRepository, PolicyMapper policyMapper, Clock clock) {
        this.policyRepository = policyRepository;
        this.policyMapper = policyMapper;
        this.clock = clock;
    }

    @Override
    public List<PolicyResult> findApplicablePolicies(PolicySearchCondition condition) {
        validate(condition);

        LocalDate today = LocalDate.now(clock);
        return policyRepository
                .findByIntentIgnoreCaseAndCustomerTypeIgnoreCaseAndProductTypeIgnoreCaseAndRequiresIdentityVerificationAndVerificationStatusIn(
                        condition.intent().trim(),
                        condition.customerType().trim(),
                        condition.productType().trim(),
                        condition.requiresIdentityVerification(),
                        List.of(VerificationStatus.OFFICIAL_SOURCE_CHECKED, VerificationStatus.VERIFIED)
                )
                .stream()
                .filter(policy -> policy.isEffectiveOn(today))
                .map(policyMapper::toResult)
                .toList();
    }

    private void validate(PolicySearchCondition condition) {
        if (condition == null
                || !StringUtils.hasText(condition.intent())
                || !StringUtils.hasText(condition.customerType())
                || !StringUtils.hasText(condition.productType())
                || condition.requiresIdentityVerification() == null) {
            throw new IllegalArgumentException("정책 검색 조건이 완전하지 않습니다.");
        }

        if ("AUTO_TRANSFER_DATE_CHANGE".equalsIgnoreCase(condition.intent())) {
            validateAutoTransferDateChange(condition);
        }
    }

    private void validateAutoTransferDateChange(PolicySearchCondition condition) {
        if (!"AUTOMATIC".equalsIgnoreCase(condition.transferMethod())) {
            throw new IllegalArgumentException("자동이체 변경 여부를 먼저 확인해야 합니다.");
        }
        if (!isValidDay(condition.currentTransferDay()) || !isValidDay(condition.requestedTransferDay())) {
            throw new IllegalArgumentException("자동이체일은 1일부터 31일 사이여야 합니다.");
        }
        if (condition.currentTransferDay().equals(condition.requestedTransferDay())) {
            throw new IllegalArgumentException("기존 이체일과 변경할 이체일이 같습니다.");
        }
    }

    private boolean isValidDay(Integer day) {
        return day != null && day >= 1 && day <= 31;
    }
}
