-- Очистка таблицы (осторожно!)
-- TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- Вставка пользователей с разными ролями
INSERT INTO users (login, password, first_name, last_name, role, created_at) VALUES 
-- Администратор
('admin', 
 '$2a$10$NkM3C8qZ7QK5X9VxLrXqZuYqX5X5X5X5X5X5X5X5X5X5X5X5X5X5', 
 'Алексей', 
 'Админов', 
 'ADMIN', 
 NOW()),

-- Менеджеры
('manager', 
 '$2a$10$NkM3C8qZ7QK5X9VxLrXqZuYqX5X5X5X5X5X5X5X5X5X5X5X5X5X5', 
 'Петр', 
 'Петров', 
 'MANAGER', 
 NOW()),

('ivanova', 
 '$2a$10$NkM3C8qZ7QK5X9VxLrXqZuYqX5X5X5X5X5X5X5X5X5X5X5X5X5X5', 
 'Елена', 
 'Иванова', 
 'MANAGER', 
 NOW()),

('sidorov', 
 '$2a$10$NkM3C8qZ7QK5X9VxLrXqZuYqX5X5X5X5X5X5X5X5X5X5X5X5X5X5', 
 'Сергей', 
 'Сидоров', 
 'MANAGER', 
 NOW())

ON CONFLICT (login) DO NOTHING;

-- Проверка созданных пользователей
SELECT id, login, first_name, last_name, role, created_at FROM users ORDER BY role, login;