CREATE TABLE consultation_policy (
    id BIGINT NOT NULL AUTO_INCREMENT,
    policy_code VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    intent VARCHAR(100) NOT NULL,
    customer_type VARCHAR(100) NOT NULL,
    product_type VARCHAR(100) NOT NULL,
    requires_identity_verification BOOLEAN NOT NULL,
    verified_content TEXT NOT NULL,
    official_page_url VARCHAR(500) NOT NULL,
    source_name VARCHAR(200) NOT NULL,
    source_url VARCHAR(500) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    verified_at DATE NULL,
    verified_by VARCHAR(100) NULL,
    verification_status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_consultation_policy_code UNIQUE (policy_code)
);

CREATE INDEX idx_consultation_policy_search
    ON consultation_policy (
        intent,
        customer_type,
        product_type,
        requires_identity_verification,
        verification_status
    );
