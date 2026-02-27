#!/bin/bash

echo "🚀 Запуск проекта BuildFlow"
echo "==========================="

# Проверка Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен!"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "❌ Docker демон не запущен!"
    echo "Запустите Docker Desktop и повторите попытку"
    exit 1
fi

echo "✅ Docker запущен"

# Проверка наличия папок
if [ ! -d "backend" ]; then
    echo "❌ Папка backend не найдена!"
    exit 1
fi

if [ ! -d "frontend" ]; then
    echo "❌ Папка frontend не найдена!"
    exit 1
fi

# Проверка наличия медиа-файлов
if [ ! -d "frontend/media" ]; then
    echo "⚠️  Папка media не найдена, создаем..."
    mkdir -p frontend/media
fi

# Остановка старых контейнеров
echo "🛑 Остановка старых контейнеров..."
docker-compose down

# Сборка и запуск
echo "🏗️  Сборка образов..."
docker-compose build --no-cache

echo "🚀 Запуск контейнеров..."
docker-compose up -d

# Ожидание запуска
echo "⏳ Ожидание запуска сервисов..."
sleep 10

# Проверка статуса
echo "📊 Статус контейнеров:"
docker-compose ps

echo ""
echo "✅ Проект запущен!"
echo "📌 Frontend: http://localhost"
echo "📌 Backend API: http://localhost:8080"
echo "📌 Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "📋 Логи: docker-compose logs -f"
echo "🛑 Остановка: docker-compose down"