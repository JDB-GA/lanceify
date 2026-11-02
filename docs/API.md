# API reference

Send requests to the gateway on port 8080. Protected endpoints require the header `Authorization: Bearer <token>`.

## Authentication

| Method | Path                 | Access | Purpose                                                              |
| ------ | -------------------- | ------ | -------------------------------------------------------------------- |
| POST   | `/api/auth/register` | Public | Register as `CLIENT` or `FREELANCER`; returns a message, not a token |
| POST   | `/api/auth/login`    | Public | Log in and receive a token                                           |

Each of these allows 5 attempts per minute from one IP address.

Register body:

```json
{
  "firstName": "Sara",
  "lastName": "Ali",
  "username": "sara",
  "email": "sara@example.com",
  "password": "Password123",
  "role": "FREELANCER"
}
```

Login body:

```json
{ "email": "sara@example.com", "password": "Password123" }
```

## Freelancer profile

| Method | Path                         | Access     | Purpose                                |
| ------ | ---------------------------- | ---------- | -------------------------------------- |
| POST   | `/api/freelancers/me`        | Freelancer | Create a draft profile                 |
| GET    | `/api/freelancers/me`        | Freelancer | View my profile and status             |
| PUT    | `/api/freelancers/me`        | Freelancer | Edit a draft or rejected profile       |
| DELETE | `/api/freelancers/me`        | Freelancer | Withdraw a profile that isn't approved |
| POST   | `/api/freelancers/me/submit` | Freelancer | Submit for admin review                |

Profile body, where `skillIds` come from `GET /api/skills`:

```json
{
  "bio": "Java developer",
  "minPrice": 50,
  "maxPrice": 500,
  "skillIds": ["SKILL_ID"]
}
```

## Application review

| Method | Path                                              | Access | Purpose                                                                  |
| ------ | ------------------------------------------------- | ------ | ------------------------------------------------------------------------ |
| GET    | `/api/admin/freelancer-applications`              | Admin  | List applications; filter with `status`; supports `page`, `size`, `sort` |
| GET    | `/api/admin/freelancer-applications/{id}`         | Admin  | View one application                                                     |
| PUT    | `/api/admin/freelancer-applications/{id}/approve` | Admin  | Approve a pending application                                            |
| PUT    | `/api/admin/freelancer-applications/{id}/reject`  | Admin  | Reject with `{ "reason": "..." }`                                        |

| Status     | Meaning                                                   |
| ---------- | --------------------------------------------------------- |
| `DRAFT`    | Created, but not yet submitted                            |
| `PENDING`  | Waiting for admin review                                  |
| `APPROVED` | Accepted by an admin                                      |
| `REJECTED` | Declined with a reason; can be edited and submitted again |

## Skills and categories

| Method | Path                          | Access    | Purpose                                             |
| ------ | ----------------------------- | --------- | --------------------------------------------------- |
| GET    | `/api/skills`                 | Logged in | List active skills; filter with `categoryId`        |
| POST   | `/api/skills`                 | Admin     | Create with `{ "name": "Java" }`                    |
| PUT    | `/api/skills/{id}`            | Admin     | Rename with `{ "name": "Java" }`                    |
| DELETE | `/api/skills/{id}`            | Admin     | Deactivate a skill                                  |
| GET    | `/api/categories`             | Logged in | List active categories                              |
| POST   | `/api/categories`             | Admin     | Create with `{ "name", "description", "skillIds" }` |
| PUT    | `/api/categories/{id}`        | Admin     | Update with `{ "name", "description" }`             |
| DELETE | `/api/categories/{id}`        | Admin     | Deactivate a category                               |
| PUT    | `/api/categories/{id}/skills` | Admin     | Replace its skills with `{ "skillIds": [] }`        |

## Proposals

| Method | Path                                | Access     | Purpose                 |
| ------ | ----------------------------------- | ---------- | ----------------------- |
| POST   | `/api/projects/{projectId}/proposals` | Freelancer | Send a proposal         |
| GET    | `/api/proposals/me`                 | Freelancer | List my proposals       |
| PUT    | `/api/proposals/{id}/withdraw`      | Freelancer | Withdraw a proposal     |

## Messaging

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/api/users/contacts/freelancers` | Client | List approved freelancers to message |
| GET | `/api/users/contacts/clients` | Freelancer | List clients to message |
| POST | `/api/conversations` | Logged in | Start or reopen a chat with `{ "recipientId": "USER_ID" }` |
| GET | `/api/conversations` | Logged in | List my conversations |
| GET | `/api/conversations/{id}/messages` | Participant | List messages and mark them read |
| POST | `/api/conversations/{id}/messages` | Participant | Send `{ "content": "..." }`, up to 3000 characters |
| GET | `/api/conversations/{id}/stream` | Participant | Receive new messages live with server-sent events |

## Status codes

| Code | Meaning                                                            |
| ---- | ------------------------------------------------------------------ |
| 400  | Invalid input, or an application that is incomplete when submitted |
| 401  | Missing, invalid or expired token, or wrong login details          |
| 403  | The wrong role for this endpoint, or a banned account at login     |
| 404  | Not found                                                          |
| 409  | Duplicate data, or an action not allowed in the current status     |
| 429  | Too many login or register attempts                                |
