ALTER TABLE transaction_authorizations ADD COLUMN reason_codes VARCHAR(1024);

INSERT INTO transaction_authorizations (transaction_id, status)
SELECT t.id, 'PENDING' FROM transaction_entity t
WHERE NOT EXISTS (
    SELECT 1 FROM transaction_authorizations a WHERE a.transaction_id = t.id
);

UPDATE transaction_authorizations
SET reason_codes = (
    SELECT reason_codes FROM transaction_entity
    WHERE transaction_entity.id = transaction_authorizations.transaction_id
);

ALTER TABLE transaction_entity DROP COLUMN reason_codes;
