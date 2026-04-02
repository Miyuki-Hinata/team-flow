INSERT IGNORE INTO users (login_id, last_name, first_name, last_name_kana, first_name_kana, email, password, level, created_at, updated_at, deleted_at)
VALUES
    ('admin', '管理者', 'ユーザー', 'かんりしゃ', 'ゆーざー', 'admin@example.com', '$2a$10$rA7A8wb1NggCqm12NlEdQOSL.Ri8kjrGSyQCQNLBwLeGaBrtLQeSy', 2, NOW(), NOW(), NULL),
    ('general', '一般', 'ユーザー', 'いっぱん', 'ゆーざー', 'general@example.com', '$2a$10$rA7A8wb1NggCqm12NlEdQOSL.Ri8kjrGSyQCQNLBwLeGaBrtLQeSy', 1, NOW(), NOW(), NULL);