CREATE TABLE payment_authorizations (
    payment_id UUID PRIMARY KEY REFERENCES payment_entity(id),
    status VARCHAR(255) NOT NULL CHECK (status IN ('PENDING', 'OK', 'NOT_OK')),
    payment_validation_status VARCHAR(255) NOT NULL
        CHECK (payment_validation_status IN ('PENDING', 'OK', 'NOT_OK'))
);

CREATE TABLE transaction_authorizations (
    transaction_id UUID PRIMARY KEY REFERENCES transaction_entity(id),
    status VARCHAR(255) NOT NULL CHECK (status IN ('PENDING', 'OK', 'NOT_OK'))
);

INSERT INTO payment_authorizations (payment_id, status, payment_validation_status)
SELECT id, COALESCE(status, 'PENDING'), COALESCE(payment_validation_status, 'PENDING')
FROM payment_entity;

INSERT INTO transaction_authorizations (transaction_id, status)
SELECT id, COALESCE(status, 'PENDING') FROM transaction_entity;

ALTER TABLE payment_entity DROP COLUMN status;
ALTER TABLE payment_entity DROP COLUMN payment_validation_status;
ALTER TABLE transaction_entity DROP COLUMN status;
