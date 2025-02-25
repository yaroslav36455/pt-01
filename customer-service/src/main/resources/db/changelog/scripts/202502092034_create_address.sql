CREATE TABLE IF NOT EXISTS address
(
    id          BIGSERIAL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    settlement  VARCHAR   NOT NULL,
    street      VARCHAR   NOT NULL,
    building    VARCHAR   NOT NULL,
    customer_id BIGINT    NOT NULL,
    CONSTRAINT pk_address_id PRIMARY KEY (id),
    CONSTRAINT fk_address_customer_id FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT uk_address_customer_id UNIQUE (customer_id)
);