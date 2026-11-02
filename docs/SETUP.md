# Setup

## Prerequisites

- Java 17
- Node.js 20 or later
- A MongoDB Atlas cluster
- Docker Desktop, only if you run with Docker

Maven is not required, because the repository includes the Maven wrapper. On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Configure MongoDB Atlas

1. Create a cluster and a database user.
2. Under **Network Access**, allow your IP address.
3. Click **Connect → Drivers** and copy the connection string.

## Create `.env`

Create this file in the repository root. All services share it. It is ignored by git, so never commit it.

```
MONGO_URI=mongodb+srv://USER:PASSWORD@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
JWT_SECRET=a-long-random-secret-of-at-least-32-characters
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=choose-a-strong-password
```

| Variable         | Required | Purpose                                                      |
| ---------------- | -------- | ------------------------------------------------------------ |
| `MONGO_URI`      | Yes      | Atlas connection; each service sets its own database name    |
| `JWT_SECRET`     | Yes      | Signs and verifies tokens; must be the same for all services |
| `ADMIN_EMAIL`    | No       | Creates the admin account on startup, if it doesn't exist    |
| `ADMIN_PASSWORD` | No       | Password for that admin account                              |

Without the two admin variables, no admin is created. Admin features and adding skills then can't be used.
