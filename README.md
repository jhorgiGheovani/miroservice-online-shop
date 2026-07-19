# Java Microservices

A microservices-based application built with Spring Boot, using Kafka for event-driven communication and PostgreSQL for each service's database.

## Services

| Service         | Port | Database Port |
| --------------- | ---- | ------------- |
| Auth Service    | -    | 5434          |
| User Service    | -    | 5433          |
| Order Service   | -    | 5435          |
| Payment Service | -    | 5436          |

## Tech Stack

- **Java / Spring Boot** — each service
- **PostgreSQL** — per-service database (DB per service pattern)
- **Apache Kafka** — async inter-service messaging
- **Docker / Docker Compose** — containerized infrastructure

## Getting Started

### Prerequisites

- Docker & Docker Compose

### Run (Production Ready)

```bash
docker compose -f docker-compose.prod.yml up --build
```

This single command will automatically:

1. Generate RSA key pair for JWT signing
2. Start Kafka and all PostgreSQL databases
3. Build and run all 4 services

### Run (Local Development)

Start infrastructure first:

```bash
docker compose up -d
```

Then run each service individually:

```bash
cd auth_service && mvn spring-boot:run
cd user_service && mvn spring-boot:run
cd order_service && mvn spring-boot:run
cd payment_service && mvn spring-boot:run
```

> Note: Local development requires Java 17+ and Maven installed.

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
