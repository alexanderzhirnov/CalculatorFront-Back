-- Очищаем таблицу users
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- Создаем админа с паролем "123"
INSERT INTO users (login, password, first_name, last_name, role, created_at) 
VALUES (
    'admin', 
    '$2a$10$Yp0Uw0Kj9Xp0Uw0Kj9Xp0Uw0Kj9Xp0Uw0Kj9Xp0Uw0Kj9Xp0U',
    'Админ',
    'Админов',
    'ADMIN',
    NOW()
);

-- Проверяем
SELECT 'Пользователь создан:' as message, id, login, role FROM users;