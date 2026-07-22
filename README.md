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
- **Apache Kafka (KRaft mode)** — async inter-service messaging, no Zookeeper required
- **Docker / Docker Compose** — containerized infrastructure

### Why KRaft over Zookeeper

This project uses Kafka in **KRaft mode** (Kafka's built-in consensus protocol) instead of the older Zookeeper-based setup.

| | KRaft (used here) | Zookeeper |
|---|---|---|
| **Ops complexity** | Single process | Two separate processes (Kafka + ZK) |
| **Extra containers** | None | Requires a Zookeeper container + port 2181 |
| **Scalability** | Supports millions of partitions | Bottlenecks around 200k partitions |
| **Status** | Kafka's present and future | Deprecated since Kafka 3.x, removed in Kafka 4.0 |

Zookeeper would only be needed for very old Kafka tooling (pre-3.x). For any new project, KRaft is the correct choice.

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

## Application Flow

### 1. Register & Login

```
Client
  │
  ├─► POST /api/users/register  ──►  User Service  ──►  user_db (save user)
  │
  └─► POST /auth/token
            │
            ├─► Auth Service  ──► (REST) ──►  User Service /api/users/validate
            │                                     └─► verify username & password
            │
            └─► sign JWT with RSA private key  ──►  return token to client
```

The auth service **never stores users** — it delegates credential validation to the user service via REST, then signs the JWT itself using its RSA private key.

---

### 2. Create an Order

```
Client
  │
  └─► POST /orders  (JWT in header)
            │
            Order Service
              ├─► verify JWT using RSA public key (in-memory, no auth service call)
              ├─► calculate grand total using Java Stream (map + reduce)
              ├─► save Order + OrderItems  ──►  order_db
              │
              └─► (REST + API Key) ──►  Payment Service POST /payments
                                            └─► create Payment record (PENDING)  ──►  payment_db
```

Order service calls payment service synchronously via REST using an internal API key (`X-Internal-Api-Key`) — this call is service-to-service only, not exposed to the client.

---

### 3. Pay or Fail a Payment

```
Client
  │
  └─► POST /payments/{paymentId}/pay  (JWT in header)
            │
            Payment Service
              ├─► verify JWT
              ├─► verify ownership (customerId in JWT must match payment's customerId)
              ├─► update Payment status  ──►  PENDING → SUCCESS (or FAILED)
              │
              └─► publish event to Kafka (payment-events topic)
                      └─► {orderId, paymentStatus, email, amount}
```

After payment, a Kafka event is published with the result. Any downstream service (e.g. notification service) can consume this event to send confirmation emails or update order status.

---

### 4. View Order History

```
Client
  │
  └─► GET /orders/my-orders  (JWT in header)
            │
            Order Service
              ├─► extract customerId from JWT claims
              ├─► run native SQL query (JOIN orders + order_items, GROUP BY, COUNT, SUM)
              └─► map results to response using Java Stream  ──►  return to client
```

---

### 5. Key Rotation (Admin)

```
Admin
  │
  └─► POST /auth/rotate-keys
            │
            Auth Service
              ├─► generate new RSA 2048-bit key pair
              ├─► replace in-memory key pair
              ├─► persist new keys to .pem files
              │
              └─► publish new public key to Kafka (key-rotation topic)
                      │
                      ├─► User Service consumer  ──►  hot-reload public key in memory
                      ├─► Order Service consumer  ──►  hot-reload public key in memory
                      └─► Payment Service consumer  ──►  hot-reload public key in memory
```

All services update their public key **without restarting**. Tokens signed with the old private key are immediately invalidated after rotation.

---

### Flow Summary Diagram

```
                              ┌─────────────┐
                              │   Client    │
                              └──┬──┬──┬──┬─┘
             ┌────────────────── ┘  │  │  └──────────────────┐
             │              ┌───────┘  └───────┐              │
       ┌─────▼──────┐ ┌─────▼──────┐ ┌─────────▼──┐ ┌────────▼───────┐
       │    Auth    │ │    User    │ │    Order   │ │    Payment    │
       │  Service   │ │  Service   │ │  Service   │ │    Service    │
       │   :8080    │ │   :8081    │ │   :8085    │ │     :8086     │
       └─────┬──────┘ └─────┬──────┘ └──┬──────┬──┘ └───────┬───────┘
             │              │            │      │             │
             └────REST──────►            │      └────REST─────►
                            │            │                    │
                       ┌────▼────┐  ┌────▼────┐         ┌────▼──────┐
                       │ user_db │  │order_db │         │payment_db │
                       │  :5433  │  │  :5435  │         │  :5436    │
                       └─────────┘  └─────────┘         └─────┬─────┘
                                                               │
                                                    Kafka: payment-events
                                                               ▼
                                                    (downstream consumers)

  Kafka: key-rotation
  Auth Service ──► User Service, Order Service, Payment Service
```

---

## API Endpoints

### Auth Service — `localhost:8080`

| Method | Endpoint            | Description                         | Auth Required |
| ------ | ------------------- | ----------------------------------- | ------------- |
| POST   | `/auth/token`       | Login & get JWT token               | No            |
| POST   | `/auth/rotate-keys` | Rotate RSA key pair for JWT signing | No            |

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "password": "secret"}'

# Rotate RSA keys
curl -X POST http://localhost:8080/auth/rotate-keys
```

#### About `/auth/rotate-keys`

This service uses RSA asymmetric keys to sign and verify JWT tokens:

- The **auth service** holds the **private key** (used to sign tokens)
- All other services (user, order, payment) hold the **public key** (used to verify tokens)

When `/auth/rotate-keys` is called:

1. A new RSA 2048-bit key pair is generated
2. The new key pair replaces the old one in memory and is persisted to the `.pem` file
3. The new **public key is published to Kafka** (`key-rotation` topic)
4. All other services consume the Kafka event and **hot-reload their public key** without restarting

This ensures that after rotation, tokens signed with the old key are immediately invalidated and all services transparently switch to verifying against the new public key.

### User Service — `localhost:8081`

| Method | Endpoint              | Description         | Auth Required |
| ------ | --------------------- | ------------------- | ------------- |
| POST   | `/api/users/register` | Register a new user | No            |

```bash
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "username": "john", "password": "secret", "email": "john@example.com"}'
```

### Order Service — `localhost:8085`

| Method | Endpoint            | Description                                      | Auth Required |
| ------ | ------------------- | ------------------------------------------------ | ------------- |
| POST   | `/orders`           | Create a new order                               | Yes (JWT)     |
| GET    | `/orders/my-orders` | Get current user's order history with item count | Yes (JWT)     |

```bash
# Create a new order
curl -X POST http://localhost:8085/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "items": [
      {"productId": "prod-001", "quantity": 2, "unitPrice": 50000},
      {"productId": "prod-002", "quantity": 1, "unitPrice": 120000}
    ]
  }'

# Get current user's order history
curl -X GET http://localhost:8085/orders/my-orders \
  -H "Authorization: Bearer <token>"
```

### Payment Service — `localhost:8086`

| Method | Endpoint                     | Description             | Auth Required |
| ------ | ---------------------------- | ----------------------- | ------------- |
| GET    | `/payments/{orderId}`        | Get payment by order ID | Yes (JWT)     |
| POST   | `/payments/{paymentId}/pay`  | Process payment         | Yes (JWT)     |
| POST   | `/payments/{paymentId}/fail` | Mark payment as failed  | Yes (JWT)     |

```bash
# Get payment by order ID
curl -X GET http://localhost:8086/payments/<orderId> \
  -H "Authorization: Bearer <token>"

# Process payment
curl -X POST http://localhost:8086/payments/<paymentId>/pay \
  -H "Authorization: Bearer <token>"

# Mark payment as failed
curl -X POST http://localhost:8086/payments/<paymentId>/fail \
  -H "Authorization: Bearer <token>"
```

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
