# Security

`identity-service` creates tokens at login. Every service with protected endpoints verifies them itself, using the shared `JWT_SECRET`.

## Token format

| Part | Value |
| --- | --- |
| Algorithm | HS256 |
| `sub` | The user's id |
| `role` | `ADMIN`, `CLIENT` or `FREELANCER`, with no `ROLE_` prefix |
| `exp` | 24 hours after login |

## Roles

| Role | How it is created |
| --- | --- |
| `CLIENT` | Public registration |
| `FREELANCER` | Public registration; an admin must approve their application |
| `ADMIN` | Created at startup from `ADMIN_EMAIL` and `ADMIN_PASSWORD` |

Protect an endpoint or controller by role:

```java
@PreAuthorize("hasRole('ADMIN')")
```

## Adding security to a service

Copy `JwtService`, `JwtAuthenticationFilter` and `SecurityConfig` from another service's `security` package, then add the security and `jjwt` dependencies and this property:

```properties
jwt.secret=${JWT_SECRET}
```

Do not annotate `JwtAuthenticationFilter` with `@Component`. Spring would register it twice, and valid tokens would get a 401.

## Calls between services

Forward the user's `Authorization` header, or the call gets a 401.

## Known gaps

- Services other than `identity-service` only read the token, so a banned user's token keeps working there until it expires.
- The frontend stores the token in browser `localStorage`.
