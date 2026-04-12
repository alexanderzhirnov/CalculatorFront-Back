ALTER TABLE users
    ADD COLUMN email VARCHAR(150),
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verification_token VARCHAR(128),
    ADD COLUMN email_verification_expires_at TIMESTAMP,
    ADD COLUMN password_reset_token VARCHAR(128),
    ADD COLUMN password_reset_expires_at TIMESTAMP;

UPDATE users
SET login = lower(btrim(login));

UPDATE users
SET email = lower(btrim(login)) || '@legacy.local'
WHERE email IS NULL OR btrim(email) = '';

UPDATE users
SET email = lower(btrim(email)),
    email_verified = TRUE;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT chk_users_login_not_blank CHECK (char_length(btrim(login)) BETWEEN 3 AND 100),
    ADD CONSTRAINT chk_users_email_not_blank CHECK (char_length(btrim(email)) BETWEEN 6 AND 150);

ALTER TABLE clients
    ADD CONSTRAINT chk_clients_last_name_not_blank CHECK (char_length(btrim(last_name)) BETWEEN 1 AND 100),
    ADD CONSTRAINT chk_clients_first_name_not_blank CHECK (char_length(btrim(first_name)) BETWEEN 1 AND 100);

ALTER TABLE calculations
    ADD CONSTRAINT chk_calculations_address_length CHECK (
        construction_address IS NULL OR char_length(btrim(construction_address)) <= 255
    );
