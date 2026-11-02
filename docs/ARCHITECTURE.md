# Architecture

The frontend calls the API gateway, which routes each request to a service. Each service owns its own database in MongoDB Atlas and verifies tokens itself.

| Component               | Port                                | Database           | Responsibility                           | Status                                                |
| ----------------------- | ----------------------------------- | ------------------ | ---------------------------------------- | ----------------------------------------------------- |
| `frontend`              | 3000 in Docker, 5173 in development |                    | React user interface                     | Login, register, freelancer application, admin review |
| `api-gateway`           | 8080                                |                    | Single entry point                       | Routing works                                         |
| `identity-service`      | 8081                                | `identity_db`      | Login, register, freelancer applications | Working                                               |
| `catalog-service`       | 8085                                | `catalog_db`       | Skills and categories                    | Working                                               |
| `marketplace-service`   | 8082                                | `marketplace_db`   | Projects, proposals, bookmarks           | Token checks only; no endpoints yet                   |
| `contract-service`      | 8083                                | `contract_db`      | Contracts, milestones, wallets, reviews  | Token checks only; no endpoints yet                   |
| `communication-service` | 8084                                | `communication_db` | Notifications, chat                      | Token checks only; no endpoints yet                   |

## Gateway routes

| Path                                                                                                   | Service                 |
| ------------------------------------------------------------------------------------------------------ | ----------------------- |
| `/api/auth/**`, `/api/users/**`, `/api/freelancers/**`, `/api/admin/freelancer-applications/**`        | `identity-service`      |
| `/api/skills/**`, `/api/categories/**`                                                                 | `catalog-service`       |
| `/api/projects/**`, `/api/proposals/**`, `/api/bookmarks/**`                                           | `marketplace-service`   |
| `/api/contracts/**`, `/api/milestones/**`, `/api/wallets/**`, `/api/reviews/**`, `/api/simulations/**` | `contract-service`      |
| `/api/notifications/**`, `/api/conversations/**`                                                       | `communication-service` |

In Docker Compose, only the frontend and the gateway publish ports.

## Frontend structure

| Folder           | Contents                                   |
| ---------------- | ------------------------------------------ |
| `src/api`        | Requests to the backend, one file per area |
| `src/context`    | Logged-in user state                       |
| `src/hooks`      | Shared loading and action logic            |
| `src/routes`     | Role-based route guards                    |
| `src/components` | Reusable interface pieces                  |
| `src/pages`      | One component per screen                   |
