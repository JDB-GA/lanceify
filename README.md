# Lanceify

Lanceify is a freelancer marketplace built with Spring Boot microservices. Clients post projects, admin-approved freelancers bid on them, and payment is held in escrow until the work is approved.

**Problem:** clients struggle to find verified people with the right skills, and freelancers have no guarantee of payment. Lanceify answers with admin vetting, skill-based search and escrow.

## Project status

| Component | Port | Database | Responsibility | Status |
| --- | --- | --- | --- | --- |
| `api-gateway` | 8080 | None | Single entry point; routes requests to services | Routing works |
| `identity-service` | 8081 | `identity_db` | Login, register, freelancer applications | Working |
| `marketplace-service` | 8082 | `marketplace_db` | Skills, categories, projects, proposals | Token checks only; no endpoints yet |
| `contract-service` | 8083 | `contract_db` | Contracts, milestones, wallets, reviews | Token checks only; no endpoints yet |
| `communication-service` | 8084 | `communication_db` | Notifications, chat | Token checks only; no endpoints yet |
| `frontend` | | | React app | Not started |

## Technologies

Java 17, Spring Boot, Spring Cloud Gateway, Spring Security, JWT, MongoDB Atlas, Docker Compose.

## Prerequisites

- Java 17
- A MongoDB Atlas cluster
- Docker Desktop, only if you run with Docker

Maven is not required, because the repository includes the Maven wrapper. On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Setup

**1. Configure Atlas**

1. Create a cluster and a database user.
2. Under **Network Access**, allow your IP address.
3. Click **Connect → Drivers** and copy the connection string.

**2. Create `.env` in the repository root**

All services share this one file. Never commit it.

```
MONGO_URI=mongodb+srv://USER:PASSWORD@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
JWT_SECRET=a-long-random-secret-of-at-least-32-characters
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=choose-a-strong-password
```

| Variable | Required | Purpose |
| --- | --- | --- |
| `MONGO_URI` | Yes | Atlas connection; each service sets its own database name |
| `JWT_SECRET` | Yes | Signs and verifies tokens; must be the same for all services |
| `ADMIN_EMAIL` | No | Creates the admin account on startup, if it doesn't exist |
| `ADMIN_PASSWORD` | No | Password for that admin account |

Without the two admin variables, no admin is created, and the admin endpoints can't be used.

## Running the project

### With Docker

```bash
docker compose up --build
```

The API is then available at `http://localhost:8080`. Only the gateway publishes a port, so all requests go through it.

```bash
docker compose down
```

### Without Docker

Run each service in its own terminal, from the repository root. To work on one feature, start only the gateway and the service you need.

```bash
./mvnw -pl identity-service spring-boot:run
```

```bash
./mvnw -pl marketplace-service spring-boot:run
```

```bash
./mvnw -pl contract-service spring-boot:run
```

```bash
./mvnw -pl communication-service spring-boot:run
```

```bash
./mvnw -pl api-gateway spring-boot:run
```

## Using the API

Send requests to the gateway on port 8080. The steps below take a freelancer from registration to admin approval.

**1. Register a freelancer.** The response contains a `token`. Clients register the same way, with the role `CLIENT`.

```bash
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"fullName":"Sara Ali","username":"sara","email":"sara@example.com","password":"Password123","role":"FREELANCER"}'
```

**2. Create the profile as a draft.** Use the freelancer's token.

```bash
curl -X POST localhost:8080/api/freelancers/me -H "Authorization: Bearer FREELANCER_TOKEN" -H "Content-Type: application/json" -d '{"bio":"Java developer","minPrice":50,"maxPrice":500,"skillIds":["java"]}'
```

**3. Submit the application to the admin.**

```bash
curl -X POST localhost:8080/api/freelancers/me/submit -H "Authorization: Bearer FREELANCER_TOKEN"
```

**4. Log in as the admin.** Use the `ADMIN_EMAIL` and `ADMIN_PASSWORD` values from `.env`.

```bash
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"admin@example.com","password":"choose-a-strong-password"}'
```

**5. List pending applications**, and copy the `id` of the one to review.

```bash
curl "localhost:8080/api/admin/freelancer-applications?status=PENDING" -H "Authorization: Bearer ADMIN_TOKEN"
```

**6. Approve the application.**

```bash
curl -X PUT localhost:8080/api/admin/freelancer-applications/APPLICATION_ID/approve -H "Authorization: Bearer ADMIN_TOKEN"
```

## API endpoints

### Authentication

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Public | Register as `CLIENT` or `FREELANCER` |
| POST | `/api/auth/login` | Public | Log in and receive a token |

Each of these allows 5 attempts per minute from one IP address.

### Freelancer application

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/freelancers/me` | Freelancer | Create a draft profile |
| GET | `/api/freelancers/me` | Freelancer | View my profile and status |
| PUT | `/api/freelancers/me` | Freelancer | Edit a draft or rejected profile |
| DELETE | `/api/freelancers/me` | Freelancer | Withdraw a profile that isn't approved |
| POST | `/api/freelancers/me/submit` | Freelancer | Submit for admin review |
| GET | `/api/admin/freelancer-applications` | Admin | List applications; filter with `status`; supports `page`, `size`, `sort` |
| GET | `/api/admin/freelancer-applications/{id}` | Admin | View one application |
| PUT | `/api/admin/freelancer-applications/{id}/approve` | Admin | Approve a pending application |
| PUT | `/api/admin/freelancer-applications/{id}/reject` | Admin | Reject with `{"reason":"..."}` |

An application moves through `DRAFT → PENDING → APPROVED` or `REJECTED`. A rejected freelancer can edit and submit again.

### Status codes

| Code | Meaning |
| --- | --- |
| 400 | Invalid input, or an application that is incomplete when submitted |
| 401 | Missing, invalid or expired token, or wrong login details |
| 403 | The wrong role for this endpoint |
| 404 | Not found |
| 409 | Duplicate data, or an action not allowed in the current status |
| 429 | Too many login or register attempts |

## Testing

```bash
./mvnw -pl identity-service test
```

The tests use mocks, so they don't need `.env` or a database.

## Troubleshooting

| Problem | Fix |
| --- | --- |
| `Port 8080 was already in use` | Stop whatever is using it; check containers with `docker ps` |
| `The connection string is invalid` | Check `MONGO_URI` in `.env`; remove any quotes or spaces |
| `Could not resolve placeholder` | A required variable is missing from `.env` |
| `Authentication failed` | Wrong database user or password in `MONGO_URI` |
| Timeout connecting to `mongodb.net` | Allow your IP address under **Network Access** in Atlas |
| 401 with a token that should work | The token expired after 24 hours, or `JWT_SECRET` differs between services |
| 502 or 503 through the gateway | The target service isn't running |

## Contributing

Never commit directly to `main`. Create a branch such as `name/feature/skills`, then open a Pull Request for review.
