# Architecture

The frontend calls the API gateway, which routes each request to a service. Each service owns its own database in MongoDB Atlas and verifies tokens itself.

| Component               | Port                                | Database           | Responsibility                           | Status                                                |
| ----------------------- | ----------------------------------- | ------------------ | ---------------------------------------- | ----------------------------------------------------- |
| `frontend`              | 3000 in Docker, 5173 in development |                    | React user interface                     | Landing, dashboards, projects, proposals, chat, admin |
| `api-gateway`           | 8080                                |                    | Single entry point                       | Routing works                                         |
| `identity-service`      | 8081                                | `identity_db`      | Login, register, freelancer applications | Working                                               |
| `catalog-service`       | 8085                                | `catalog_db`       | Skills and categories                    | Working                                               |
| `marketplace-service`   | 8082                                | `marketplace_db`   | Projects, proposals, bookmarks           | Token checks only; no endpoints yet                   |
| `contract-service`      | 8083                                | `contract_db`      | Contracts, milestones, wallets, reviews  | Token checks only; no endpoints yet                   |
| `communication-service` | 8084                                | `communication_db` | Notifications, chat                      | Conversations, messages, live streams                 |
| `proposal-service`      | 8086                                | `proposal_db`      | Proposals on projects                    | Apply, withdraw, hire, decline                        |

## Gateway routes

| Path                                                                                                   | Service                 |
| ------------------------------------------------------------------------------------------------------ | ----------------------- |
| `/api/auth/**`, `/api/users/**`, `/api/freelancers/**`, `/api/admin/freelancer-applications/**`        | `identity-service`      |
| `/api/skills/**`, `/api/categories/**`                                                                 | `catalog-service`       |
| `/api/proposals/**`, `/api/projects/*/proposals`                                                       | `proposal-service`      |
| `/api/projects/**`, `/api/bookmarks/**`                                                                | `marketplace-service`   |
| `/api/contracts/**`, `/api/milestones/**`, `/api/wallets/**`, `/api/reviews/**`, `/api/simulations/**` | `contract-service`      |
| `/api/notifications/**`, `/api/conversations/**`                                                       | `communication-service` |

In Docker Compose, only the frontend and the gateway publish ports.

## Live chat

Chat updates reach the browser over server-sent events. When you open the messages page, the browser keeps a connection open to `communication-service`, and new messages and read receipts arrive on it as they happen.

Open connections don't hold a thread each, so lots of idle tabs cost very little. The work happens when something is sent: the service writes the event to every open tab for that thread or user. That job runs on a small pool of `sse-` threads (4 to 8), so the person who sent the message gets their response straight away instead of waiting on everyone else's connection. If one tab is slow or has gone away, it only holds up that pool, not the chat request. Every 25 seconds the pool also sends a small ping so proxies don't close quiet connections.

If the pool ever falls far behind, the request that sent the message delivers its own events. That slows the sender down a little rather than dropping messages.

## Frontend structure

| Folder           | Contents                                   |
| ---------------- | ------------------------------------------ |
| `src/api`        | Requests to the backend, one file per area |
| `src/context`    | Logged-in user state                       |
| `src/hooks`      | Shared loading and action logic            |
| `src/routes`     | Role-based route guards                    |
| `src/components` | Reusable interface pieces                  |
| `src/pages`      | One component per screen                   |
