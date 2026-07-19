# Java Microservices

A microservices-based application built with Spring Boot, using Kafka for event-driven communication and PostgreSQL for each service's database.

## Services

| Service | Port | Database Port |
|---|---|---|
| Auth Service | - | 5434 |
| User Service | - | 5433 |
| Order Service | - | 5435 |
| Payment Service | - | 5436 |

## Tech Stack

- **Java / Spring Boot** — each service
- **PostgreSQL** — per-service database (DB per service pattern)
- **Apache Kafka** — async inter-service messaging
- **Docker / Docker Compose** — containerized infrastructure

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17+
- Maven

### Run Infrastructure

```bash
docker compose up -d
```

This starts Kafka and all PostgreSQL databases.

### Run Services

Start each service individually from its directory:

```bash
cd auth_service && mvn spring-boot:run
cd user_service && mvn spring-boot:run
cd order_service && mvn spring-boot:run
cd payment_service && mvn spring-boot:run
```

## Project Structure

```
microservices/
├── auth_service/       # Authentication & JWT
├── user_service/       # User management
├── order_service/      # Order processing
├── payment_service/    # Payment processing
├── docker-compose.yml
└── docker-compose.prod.yml
```
