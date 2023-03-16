CREATE TABLE certificate_authority (
    id BIGSERIAL PRIMARY KEY,
    certificate_hash TEXT NOT NULL,
    certificate_pem TEXT NOT NULL,
    subject TEXT NOT NULL,
    issuer TEXT NOT NULL,
    valid TIMESTAMPTZ NOT NULL,
    expires TIMESTAMPTZ NOT NULL,
    created_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX certificate_authority_certificate_hash_idx ON certificate_authority (certificate_hash);
