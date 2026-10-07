.PHONY: up down logs test build ps seed-check

up:
	docker compose up --build -d

down:
	docker compose down

logs:
	docker compose logs -f app

test:
	./mvnw test

build:
	docker compose build app

ps:
	docker compose ps

seed-check:
	curl -s "http://localhost:4000/doctors?size=2" | head -c 500; echo
