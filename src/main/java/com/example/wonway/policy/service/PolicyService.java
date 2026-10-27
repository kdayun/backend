package com.example.wonway.policy.service;

import com.example.wonway.common.contract.PolicyResult;
import com.example.wonway.common.contract.PolicySearchCondition;

import java.util.List;

/**
 * 담당 B가 구현하는 상담 정책 조회 및 검증 계약입니다.
 */
public interface PolicyService {

    List<PolicyResult> findApplicablePolicies(PolicySearchCondition condition);
}
