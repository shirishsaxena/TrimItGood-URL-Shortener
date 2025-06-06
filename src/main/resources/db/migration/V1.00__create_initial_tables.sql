-- URL table stores the original and shortened URL data
CREATE TABLE IF NOT EXISTS url (
    id SERIAL PRIMARY KEY,
    short_code VARCHAR(20) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP,
    access_limit INT
);

-- Stats table logs access metadata
CREATE TABLE IF NOT EXISTS url_stats (
    id SERIAL PRIMARY KEY,
    url_id INT NOT NULL,
    ip_access_from VARCHAR(45),
    user_agent VARCHAR(100),
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_url
        FOREIGN KEY (url_id)
        REFERENCES url (id)
        ON DELETE CASCADE
);

--CREATE SEQUENCE IF NOT EXISTS url_seq
--    START WITH 100000000000;

CREATE TABLE IF NOT EXISTS url_sequence (
    id BIGINT PRIMARY KEY,
    curr_no BIGINT NOT NULL
);

-- Insert starting value
INSERT INTO url_sequence (id, curr_no) VALUES (1, 100000000000)
