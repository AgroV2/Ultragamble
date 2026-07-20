ALTER TABLE bot_users
    ADD COLUMN ai_provider VARCHAR(255) DEFAULT 'ANYMODEL',
    ADD COLUMN ai_model    VARCHAR(255) DEFAULT 'cc/claude-opus-4-8';