CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users (id),
    created_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER devices_update_modified_on
BEFORE UPDATE ON devices
FOR EACH ROW
EXECUTE PROCEDURE update_modified_on();

CREATE INDEX devices_user_id_idx ON devices (user_id);
