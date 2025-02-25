CREATE TABLE IF NOT EXISTS storage_resource
(
    id           BIGSERIAL,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL,
    uuid         UUID      NOT NULL,
    category     VARCHAR   NOT NULL,
    bucket       VARCHAR   NOT NULL,
    content_type VARCHAR,
    title        VARCHAR,
    path         VARCHAR   NOT NULL,
    CONSTRAINT primary_key_id PRIMARY KEY (id),
    CONSTRAINT unique_uuid UNIQUE (uuid),
    CONSTRAINT unique_path UNIQUE (path)
);
