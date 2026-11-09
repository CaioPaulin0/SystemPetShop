-- MySQL 8+: execute this script separately in BOTH source databases:
-- app.datasource.main (systempetshop) and app.datasource.productexpired (systemExpired).
-- Select the database before executing. No outbox is needed in productmetrics.
-- All timestamps are UTC. The application does not execute this script automatically.
CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    topic VARCHAR(249) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    payload LONGTEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    next_attempt_at DATETIME(6) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(255) NULL,
    published_at DATETIME(6) NULL,
    CONSTRAINT uk_outbox_event_id UNIQUE (event_id),
    INDEX idx_outbox_pending (published_at, next_attempt_at, id)
) ENGINE=InnoDB;
