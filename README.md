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

## API Endpoints

### Auth Service — `localhost:8080`

| Method | Endpoint | Description | Auth Required |
| ------ | -------- | ----------- | ------------- |
| POST | `/auth/token` | Login & get JWT token | No |

### User Service — `localhost:8081`

| Method | Endpoint | Description | Auth Required |
| ------ | -------- | ----------- | ------------- |
| POST | `/api/users/register` | Register a new user | No |

### Order Service — `localhost:8085`

| Method | Endpoint | Description | Auth Required |
| ------ | -------- | ----------- | ------------- |
| POST | `/orders` | Create a new order | Yes (JWT) |
| GET | `/orders/my-orders` | Get current user's order history with item count | Yes (JWT) |

### Payment Service — `localhost:8086`

| Method | Endpoint | Description | Auth Required |
| ------ | -------- | ----------- | ------------- |
| GET | `/payments/{paymentId}` | Get payment by ID | Yes (JWT) |
| POST | `/payments/{paymentId}/pay` | Process payment | Yes (JWT) |
| POST | `/payments/{paymentId}/fail` | Mark payment as failed | Yes (JWT) |

> Endpoints marked **Yes (JWT)** require `Authorization: Bearer <token>` header.

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
