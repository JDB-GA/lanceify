# Lanceify

Lanceify is a freelancer marketplace built with Spring Boot microservices and React. Clients post projects, admin-approved freelancers bid on them, and payment is held in escrow until the work is approved.

**Problem:** clients struggle to find verified people with the right skills, and freelancers have no guarantee of payment. Lanceify answers with admin vetting, skill-based search and escrow.

## Quick start

1. Create `.env` in the repository root, as described in [Setup](docs/SETUP.md).
2. Start everything:

```bash
docker compose up --build
```

3. Open `http://localhost:3000`.

## Documentation

| Guide | Contents |
| --- | --- |
| [Setup](docs/SETUP.md) | Prerequisites, MongoDB Atlas and environment variables |
| [Running](docs/RUNNING.md) | Docker, individual services and the frontend |
| [Architecture](docs/ARCHITECTURE.md) | Services, ports, databases and project status |
| [API reference](docs/API.md) | Endpoints, statuses and error codes |
| [Security](docs/SECURITY.md) | JWT format, roles and adding security to a service |
| [Development](docs/DEVELOPMENT.md) | Testing, the approval flow and contributing |
| [Troubleshooting](docs/TROUBLESHOOTING.md) | Common problems and fixes |

## Technologies

| Area | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot, Spring Cloud Gateway |
| Frontend | React, TypeScript, Vite |
| Security | Spring Security, JWT |
| Database | MongoDB Atlas |
| DevOps | Docker, Docker Compose |
