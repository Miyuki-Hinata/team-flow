INSERT IGNORE INTO users (login_id, last_name, first_name, last_name_kana, first_name_kana, email, password, level, role, created_at, updated_at, deleted_at)
VALUES
    ('admin', '管理者', 'ユーザー', 'かんりしゃ', 'ゆーざー', 'admin@example.com', '$2a$10$rA7A8wb1NggCqm12NlEdQOSL.Ri8kjrGSyQCQNLBwLeGaBrtLQeSy', 2, 'DOCTOR', NOW(), NOW(), NULL),
    ('general', '一般', 'ユーザー', 'いっぱん', 'ゆーざー', 'general@example.com', '$2a$10$rA7A8wb1NggCqm12NlEdQOSL.Ri8kjrGSyQCQNLBwLeGaBrtLQeSy', 1, 'NURSE', NOW(), NOW(), NULL);



# 患者一覧APIを叩いて、doctorにroleが含まれるか確認
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc3OTU3OTMwNywiZXhwIjoxNzc5NjY1NzA3fQ.QZuzSDxcioTCJZjWrTC9UIYRNShMDT1eb-bj3xQmDi0" http://localhost:8080/api/patients | json_pp

eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc3OTU3OTMwNywiZXhwIjoxNzc5NjY1NzA3fQ.QZuzSDxcioTCJZjWrTC9UIYRNShMDT1eb-bj3xQmDi0