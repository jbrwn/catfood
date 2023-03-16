CREATE TABLE certificates (
    id BIGSERIAL PRIMARY KEY,
    certificate_hash TEXT NOT NULL,
    certificate_pem TEXT NOT NULL,
    subject TEXT NOT NULL,
    certificate_authority_id BIGINT NOT NULL REFERENCES certificate_authority (id),
    valid TIMESTAMPTZ NOT NULL,
    expires TIMESTAMPTZ NOT NULL,
    status TEXT NOT NULL,
    device_id BIGINT NOT NULL REFERENCES devices (id),
    created_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER certificates_update_modified_on
BEFORE UPDATE ON certificates
FOR EACH ROW
EXECUTE PROCEDURE update_modified_on();

CREATE INDEX certificates_certificate_hash_idx ON certificates (certificate_hash);
