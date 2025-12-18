-- Add gender column to customer_profile (default 'N': Not specified; 'F': Female; 'M': Male)
ALTER TABLE customer_profile
    ADD COLUMN IF NOT EXISTS gender VARCHAR(1) DEFAULT 'N';

-- Add login_log table for tracking user logins
CREATE TABLE IF NOT EXISTS login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    logged_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    source VARCHAR(100),
    CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);



