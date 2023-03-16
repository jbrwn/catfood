CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES devices (id),
    longitude DOUBLE PRECISION NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    altitude DOUBLE PRECISION NOT NULL,
    speed DOUBLE PRECISION NOT NULL,
    angle DOUBLE PRECISION NOT NULL,
    magneticVariation DOUBLE PRECISION NOT NULL,
    recorded_on TIMESTAMPTZ NOT NULL,
    created_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX locations_device_id_recorded_on_idx ON locations (device_id, recorded_on);
