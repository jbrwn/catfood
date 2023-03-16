CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER users_update_modified_on
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE PROCEDURE update_modified_on();

CREATE INDEX users_username_idx ON users (username);