@echo off
echo Запуск проекта Calculator...
echo.

REM Проверка Docker
docker ps >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Docker не запущен!
    echo.
    echo Решение:
    echo 1. Нажмите Win + R, введите: services.msc
    echo 2. Найдите "Docker Desktop Service"
    echo 3. Нажмите правой кнопкой - "Запустить"
    echo 4. Или просто запустите Docker Desktop из меню Пуск
    echo.
    echo После запуска Docker подождите 1-2 минуты и запустите этот файл снова
    pause
    exit /b 1
)

echo [OK] Docker запущен

REM Проверка наличия репозиториев
if not exist "backend" (
    echo Клонируем backend...
    git clone https://github.com/alexanderzhirnov/CalculatorBackend.git backend
) else (
    echo [OK] Backend найден
)

if not exist "frontend" (
    echo Клонируем frontend...
    git clone https://github.com/alexanderzhirnov/CalculatorFrontend.git frontend
) else (
    echo [OK] Frontend найден
)

echo.
echo Собираем и запускаем контейнеры...
docker-compose up -d --build

echo.
echo Контейнеры запущены:
docker-compose ps

echo.
echo Проект доступен: http://localhost
echo Для остановки: docker-compose down
echo Для просмотра логов: docker-compose logs -f
echo.

pause