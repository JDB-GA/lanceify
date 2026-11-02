# Development

## Testing

These tests use mocks, so they don't need `.env` or a database.

```bash
./mvnw -pl identity-service test
```

Check the frontend's types and build:

```bash
cd frontend && npm run build
```

## Trying the approval flow

Use the frontend, or the steps below against the gateway.

**1. Log in as the admin**, and copy the `token`.

```bash
curl -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"choose-a-strong-password"}'
```

**2. Create a skill**, and copy its `id`. Freelancers can't submit without one.

```bash
curl -X POST localhost:8080/api/skills \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Java"}'
```

**3. Register a freelancer.**

```bash
curl -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Sara","lastName":"Ali","username":"sara","email":"sara@example.com","password":"Password123","role":"FREELANCER"}'
```

**4. Log in as the freelancer**, and copy the `token`.

```bash
curl -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sara@example.com","password":"Password123"}'
```

**5. Create a draft profile.**

```bash
curl -X POST localhost:8080/api/freelancers/me \
  -H "Authorization: Bearer FREELANCER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bio":"Java developer","minPrice":50,"maxPrice":500,"skillIds":["SKILL_ID"]}'
```

**6. Submit the application.**

```bash
curl -X POST localhost:8080/api/freelancers/me/submit \
  -H "Authorization: Bearer FREELANCER_TOKEN"
```

**7. List pending applications**, and copy the `id`.

```bash
curl "localhost:8080/api/admin/freelancer-applications?status=PENDING" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

**8. Approve the application.**

```bash
curl -X PUT localhost:8080/api/admin/freelancer-applications/APPLICATION_ID/approve \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## Contributing

Never commit directly to `main`. Create a branch such as `name/feature/skills`, then open a Pull Request for review.
