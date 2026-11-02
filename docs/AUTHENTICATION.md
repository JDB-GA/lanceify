# Authentication Handover

Token checking is already done in all four services. **What is missing is creating tokens:** the `User` model, register and login.

All of this goes in `identity-service`, package `com.ga.lanceify.identity`.

## What to build

| Endpoint | Access | Behaviour |
| --- | --- | --- |
| `POST /api/auth/register` | Public | Creates a `CLIENT` or `FREELANCER` user. Never allow `ADMIN` here. Duplicate email returns 409. |
| `POST /api/auth/login` | Public | Returns a JWT. Wrong credentials return 401. Banned users cannot log in. |

You also need:

- `User` model in the `users` collection: `email` (unique), `passwordHash`, `name`, `role`, `banned`
- `PasswordEncoder` bean using BCrypt
- Token creation added to the existing `JwtService`, which only parses tokens today
- A seeded admin account, because admins cannot register publicly

`/api/auth/**` is already public in `SecurityConfig`, and the gateway already routes it to this service.

## Token format

Tokens must match this exactly, or every service will reject them.

| Part | Value |
| --- | --- |
| Algorithm | HS256 |
| Signing secret | `JWT_SECRET` from `.env`, at least 32 characters |
| `sub` | The user's id, not their email |
| `role` | `ADMIN`, `CLIENT` or `FREELANCER`, with no `ROLE_` prefix |
| `exp` | Always set an expiry; none is enforced if it is missing |

Clients send the token as `Authorization: Bearer <token>`.

```java
Jwts.builder()
		.subject(user.getId())
		.claim("role", user.getRole().name())
		.expiration(expiry)
		.signWith(key)
		.compact();
```

## What already exists

| Piece | Location |
| --- | --- |
| Security rules | `security/SecurityConfig.java` in each service |
| Token parsing | `security/JwtService.java` in each service |
| Token filter | `security/JwtAuthenticationFilter.java` in each service |
| Current user id | `security/JwtCurrentUserProvider.java` in `identity-service` |

Protect endpoints by role with `@PreAuthorize("hasRole('ADMIN')")`.

Do not annotate `JwtAuthenticationFilter` with `@Component`. Spring would register it twice, and valid tokens would get a 401.

## Known gaps

- **Banned users:** an existing token keeps working until it expires, because the filter only reads the token. The requirements say it must be rejected. Add this check in `identity-service`, and decide how the other services should handle it.
- **Calls between services:** forward the user's `Authorization` header, or the call gets a 401.
- **Other services:** they have no current-user helper yet. Read the user id from `SecurityContextHolder`, as `JwtCurrentUserProvider` does.

## Setup

Create `.env` in the repo root. It is ignored by git, so never commit it.

```
MONGO_URI=mongodb+srv://USER:PASSWORD@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
JWT_SECRET=your-long-random-secret-at-least-32-characters
```

Run and test:

```bash
./mvnw -pl identity-service spring-boot:run
./mvnw -pl identity-service test
```

## Done when

1. Register and login work, and passwords are stored only as BCrypt hashes.
2. A login token works on `POST /api/freelancers/me` for a `FREELANCER`.
3. A `FREELANCER` token gets 403 on `/api/admin/freelancer-applications`.
4. An `ADMIN` token can approve an application.
5. No token gets 401.
