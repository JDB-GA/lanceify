# Troubleshooting

| Problem                             | Fix                                                                        |
| ----------------------------------- | -------------------------------------------------------------------------- |
| `Port 8080 was already in use`      | Stop whatever is using it; check containers with `docker ps`               |
| `The connection string is invalid`  | Check `MONGO_URI` in `.env`; remove any quotes or spaces                   |
| `Could not resolve placeholder`     | A required variable is missing from `.env`                                 |
| `Authentication failed`             | Wrong database user or password in `MONGO_URI`                             |
| Timeout connecting to `mongodb.net` | Allow your IP address under **Network Access** in Atlas                    |
| 401 with a token that should work   | The token expired after 24 hours, or `JWT_SECRET` differs between services |
| 403 when creating a skill           | Log in as the admin; only admins can change the catalog                    |
| 429 on login or register            | Wait one minute; only 5 attempts per minute are allowed                    |
| 502 or 503 through the gateway      | The target service isn't running                                           |
| No skills in the freelancer form    | Start `catalog-service`, then create skills as the admin                   |
