package com.example.wonway.policy.repository;

import com.example.wonway.policy.domain.ConsultationPolicy;
import com.example.wonway.policy.domain.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyRepository extends JpaRepository<ConsultationPolicy, Long> {

    List<ConsultationPolicy> findByIntentIgnoreCaseAndCustomerTypeIgnoreCaseAndProductTypeIgnoreCaseAndRequiresIdentityVerificationAndVerificationStatus(
            String intent,
            String customerType,
            String productType,
            boolean requiresIdentityVerification,
            VerificationStatus verificationStatus
    );
}
