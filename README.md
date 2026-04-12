# Building Calculator

## Запуск в Docker

1. Пересоберите фронтенд:

```bash
cd frontend
npm run build
cd ..
```

2. Поднимите стек:

```bash
docker compose up --build
```

3. Откройте приложение:

- фронт: `http://127.0.0.1:3000`
- API через фронт-прокси: `http://127.0.0.1:3000/api/...`
- Swagger UI: `http://127.0.0.1:3000/swagger-ui.html`
- MailHog: `http://127.0.0.1:8025`

4. Остановите контейнеры:

```bash
docker compose down
```

## Что внутри

- `db` — PostgreSQL с постоянным volume `postgres_data`
- `mailhog` — локальный SMTP/Web UI для подтверждения почты и восстановления пароля
- `back` — Spring Boot backend во внутренней сети Docker, наружу не публикуется
- `frontend` — собранный Vite/React в `nginx`, наружу открыт только он, а `/api` проксируется в backend

## Переменные окружения

- `APP_JWT_SECRET` — секрет для JWT
- `APP_CORS_ALLOWED_ORIGINS` — список разрешённых origin через запятую
- `APP_PUBLIC_BASE_URL` — публичный адрес приложения для ссылок из писем
- `APP_SUPPORT_TELEGRAM_BOT_TOKEN` — токен Telegram-бота для поддержки
- `APP_SUPPORT_TELEGRAM_CHAT_ID` — chat id для поддержки

Если `APP_SUPPORT_TELEGRAM_BOT_TOKEN` или `APP_SUPPORT_TELEGRAM_CHAT_ID` не заданы, обращения из формы поддержки принимаются backend'ом в безопасном `log`-режиме без утечки секретов во фронт.

## Проверка проекта

Быстрые backend-тесты:

```bash
cd back
./mvnw test
```

Сквозной smoke-тест поднятого Docker-стека:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```
