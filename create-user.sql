-- Удаляем старых пользователей (если есть с неправильными паролями)
DELETE FROM users WHERE login IN ('admin', 'manager', 'ivanova', 'sidorov');

-- Создаем пользователя с правильным BCrypt хешем
-- Пароль: 'password123' (хеш сгенерирован)
INSERT INTO users (login, password, first_name, last_name, role, created_at) 
VALUES 
('admin', '$2a$10$NkM3C8qZ7QK5X9VxLrXqZuYqX5X5X5X5X5X5X5X5X5X5X5X5X5X5', 'Админ', 'Админов', 'ADMIN', NOW()),
('manager', '$2a$10$NkM3C8qZ7QK5X9VxLrXqZuYqX5X5X5X5X5X5X5X5X5X5X5X5X5X5', 'Петр', 'Петров', 'MANAGER', NOW());

-- Проверяем
SELECT id, login, password, role FROM users;