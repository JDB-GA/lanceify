# Running

Complete [Setup](SETUP.md) first.

## With Docker

```bash
docker compose up --build
```

| Address                 | What it serves |
| ----------------------- | -------------- |
| `http://localhost:3000` | Frontend       |
| `http://localhost:8080` | API gateway    |

Stop everything:

```bash
docker compose down
```

## Without Docker

Run each command in its own terminal, from the repository root. To work on one feature, start only the gateway and the services it needs.

**API gateway**

```bash
./mvnw -pl api-gateway spring-boot:run
```

**Identity service**

```bash
./mvnw -pl identity-service spring-boot:run
```

**Catalog service**

```bash
./mvnw -pl catalog-service spring-boot:run
```

**Marketplace service**

```bash
./mvnw -pl marketplace-service spring-boot:run
```

**Proposal service**

```bash
./mvnw -pl proposal-service spring-boot:run
```

**Project service**

```bash
./mvnw -pl project-service spring-boot:run
```

**Contract service**

```bash
./mvnw -pl contract-service spring-boot:run
```

**Communication service**

```bash
./mvnw -pl communication-service spring-boot:run
```

`-pl` tells Maven which module to run.

## Frontend

```bash
cd frontend
```

```bash
npm install
```

```bash
npm run dev
```

Open `http://localhost:5173`. Requests to `/api` go to the gateway on port 8080. To use another address, set `API_PROXY_TARGET` before starting.
