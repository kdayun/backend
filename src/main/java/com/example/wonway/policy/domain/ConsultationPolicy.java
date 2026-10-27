package com.example.wonway.policy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "consultation_policy")
public class ConsultationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_code", nullable = false, unique = true, length = 100)
    private String policyCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String intent;

    @Column(name = "customer_type", nullable = false, length = 100)
    private String customerType;

    @Column(name = "product_type", nullable = false, length = 100)
    private String productType;

    @Column(name = "requires_identity_verification", nullable = false)
    private boolean requiresIdentityVerification;

    @Column(name = "verified_content", nullable = false, columnDefinition = "TEXT")
    private String verifiedContent;

    @Column(name = "official_page_url", nullable = false, length = 500)
    private String officialPageUrl;

    @Column(name = "source_name", nullable = false, length = 200)
    private String sourceName;

    @Column(name = "source_url", nullable = false, length = 500)
    private String sourceUrl;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "verified_at")
    private LocalDate verifiedAt;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private VerificationStatus verificationStatus;

    protected ConsultationPolicy() {
    }

    public ConsultationPolicy(
            String policyCode,
            String title,
            String intent,
            String customerType,
            String productType,
            boolean requiresIdentityVerification,
            String verifiedContent,
            String officialPageUrl,
            String sourceName,
            String sourceUrl,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            LocalDate verifiedAt,
            String verifiedBy,
            VerificationStatus verificationStatus
    ) {
        this.policyCode = policyCode;
        this.title = title;
        this.intent = intent;
        this.customerType = customerType;
        this.productType = productType;
        this.requiresIdentityVerification = requiresIdentityVerification;
        this.verifiedContent = verifiedContent;
        this.officialPageUrl = officialPageUrl;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.verifiedAt = verifiedAt;
        this.verifiedBy = verifiedBy;
        this.verificationStatus = verificationStatus;
    }

    public boolean isEffectiveOn(LocalDate date) {
        boolean started = !effectiveFrom.isAfter(date);
        boolean notEnded = effectiveTo == null || !effectiveTo.isBefore(date);
        return started && notEnded;
    }

    public Long getId() {
        return id;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public String getTitle() {
        return title;
    }

    public String getVerifiedContent() {
        return verifiedContent;
    }

    public String getOfficialPageUrl() {
        return officialPageUrl;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }
}
