.PHONY: help build up down logs clean

help:
	@echo "Доступные команды:"
	@echo "  make build  - Собрать все образы"
	@echo "  make up     - Запустить все контейнеры"
	@echo "  make down   - Остановить все контейнеры"
	@echo "  make logs   - Показать логи"
	@echo "  make clean  - Очистить всё (контейнеры, volumes)"

build:
	docker-compose build

up:
	docker-compose up -d

down:
	docker-compose down

logs:
	docker-compose logs -f

clean:
	docker-compose down -v
	docker system prune -f