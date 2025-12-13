INSERT INTO role (id, name) VALUES (1, 'ADMIN') ON CONFLICT (id) DO NOTHING;
INSERT INTO user (id, username, password, email, role_id)
VALUES (1, 'admin', '$2a$10$..................', 'anh.tuan.08355@gmail.com', 1)
    ON CONFLICT (id) DO NOTHING;