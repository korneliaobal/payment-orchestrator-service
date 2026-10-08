CREATE TABLE payment_entity (
    id UUID PRIMARY KEY,
    status VARCHAR(255),
    payment_validation_status VARCHAR(255),
    debtor_name VARCHAR(255),
    debtor_account_number VARCHAR(255),
    currency VARCHAR(255),
    total_amount NUMERIC(38, 8),
    transaction_count INTEGER,
    created_at TIMESTAMP(6) WITH TIME ZONE,
    reason_codes VARCHAR(1024)
);

CREATE TABLE transaction_entity (
    id UUID PRIMARY KEY,
    payment_id UUID,
    status VARCHAR(255),
    creditor_name VARCHAR(255),
    creditor_account_number VARCHAR(255),
    amount NUMERIC(38, 8),
    reason_codes VARCHAR(1024)
);
