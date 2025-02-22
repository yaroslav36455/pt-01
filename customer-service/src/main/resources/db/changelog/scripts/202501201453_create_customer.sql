CREATE TABLE customer
(
    id         BIGSERIAL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    first_name VARCHAR   NOT NULL,
    last_name  VARCHAR   NOT NULL,
    birth_date DATE      NOT NULL,
    phone      VARCHAR   NOT NULL,
    email      VARCHAR   NOT NULL,
    document   UUID,
    CONSTRAINT pk_customer_id PRIMARY KEY (id),
    CONSTRAINT uk_customer_phone UNIQUE (phone),
    CONSTRAINT uk_customer_email UNIQUE (email),
    CONSTRAINT uk_customer_document UNIQUE (document),
    CONSTRAINT check_customer_phone CHECK (phone ~* '^\+3\d{2}\(\d{2}\)\d{3}-\d{2}-\d{2}$'),
    CONSTRAINT check_customer_email CHECK (email ~* '^[A-Za-z0-9._+%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$')
);
