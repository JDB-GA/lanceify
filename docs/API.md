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

## Users

| Method | Path                  | Access    | Purpose                                                   |
| ------ | --------------------- | --------- | --------------------------------------------------------- |
| GET    | `/api/users?ids=a,b`  | Logged in | Names and usernames for up to 100 user IDs                |
| GET    | `/api/users/contacts/freelancers` | Client | Approved freelancers to message |
| GET    | `/api/users/contacts/clients` | Freelancer | Clients to message |

## Projects

| Method | Path                                        | Access       | Purpose                                              |
| ------ | ------------------------------------------- | ------------ | ---------------------------------------------------- |
| POST   | `/api/projects`                             | Client       | Post a project                                       |
| GET    | `/api/projects`                             | Logged in    | Open projects; filter with `categoryId` or `skillIds` |
| GET    | `/api/projects/my`                          | Client       | My projects                                          |
| GET    | `/api/projects/assigned`                    | Freelancer   | Projects I was hired for                             |
| GET    | `/api/projects/{id}`                        | Logged in    | One project                                          |
| PUT    | `/api/projects/{id}`                        | Client       | Edit while `OPEN`                                    |
| PATCH  | `/api/projects/{id}/status`                 | Client       | Change status with `{ "status" }`                    |
| PATCH  | `/api/projects/{id}/assign/{freelancerId}`  | Client       | Assign a freelancer                                  |
| DELETE | `/api/projects/{id}`                        | Client, Admin | Delete                                              |

Post body:

```json
{
  "title": "Landing page for my bakery",
  "description": "What should be delivered...",
  "budget": 800,
  "deadline": "2026-12-01",
  "categoryId": "CATEGORY_ID",
  "requiredSkillIds": ["SKILL_ID"]
}
```

## Projects

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/projects` | Client | Post a project |
| GET | `/api/projects` | Logged in | Browse open projects; filter with `categoryId`, `skillIds` |
| GET | `/api/projects/my` | Client | List my projects |
| GET | `/api/projects/assigned` | Freelancer | List projects I was hired for |
| GET | `/api/projects/{id}` | Logged in | View a project |
| PUT | `/api/projects/{id}` | Client | Edit my open project |
| PATCH | `/api/projects/{id}/status` | Client | Change my project's status |
| PATCH | `/api/projects/{id}/assign/{freelancerId}` | Client | Hire a freelancer |
| DELETE | `/api/projects/{id}` | Client or admin | Delete a project |

## Proposals

| Method | Path                                 | Access     | Purpose                                                        |
| ------ | ------------------------------------ | ---------- | -------------------------------------------------------------- |
| POST   | `/api/projects/{projectId}/proposals` | Freelancer | Apply with `{ "coverLetter", "bidAmount", "deliveryDays" }`    |
| GET    | `/api/proposals/me`                  | Freelancer | My proposals                                                   |
| PUT    | `/api/proposals/{id}/withdraw`       | Freelancer | Withdraw a pending proposal                                    |
| GET    | `/api/proposals/received`            | Client     | Proposals on all my projects                                   |
| GET    | `/api/projects/{projectId}/proposals` | Client     | Proposals on one of my projects                                |
| PUT    | `/api/proposals/{id}/accept`         | Client     | Hire; declines the other pending proposals; optional `version` |
| PUT    | `/api/proposals/{id}/reject`         | Client     | Decline a pending proposal; optional `version`                 |

## Conversations

| Method | Path                               | Access    | Purpose                                                          |
| ------ | ---------------------------------- | --------- | ---------------------------------------------------------------- |
| POST   | `/api/conversations`               | Logged in | Start or reuse a thread with `{ "recipientId", "projectId" }`    |
| GET    | `/api/conversations`               | Logged in | My threads, newest first, with unread counts                     |
| GET    | `/api/conversations/unread`        | Logged in | Total unread messages                                            |
| GET    | `/api/conversations/stream`        | Logged in | Server-sent events for all my threads: `NEW_MESSAGE`, `READ`     |
| GET    | `/api/conversations/{id}/messages` | Participant | Messages; marks received ones as read                          |
| POST   | `/api/conversations/{id}/messages` | Participant | Send `{ "content", "clientMessageId" }`                        |
| PATCH  | `/api/conversations/{id}/read`     | Participant | Mark the thread as read                                        |
| GET    | `/api/conversations/{id}/stream`   | Participant | Server-sent events for one thread                              |

## Concurrency

| Situation | Protection |
| --- | --- |
| Two clients hire on the same project at once | Unique partial index allows one `ACCEPTED` proposal per project; the second gets 409 |
| Freelancer withdraws while the client decides | `@Version` on proposals; the later write gets 409 |
| Two admins edit the same skill or category | `@Version`, plus the `version` the editor loaded; stale edits get 409 |
| Two skills or categories with the same name | Case-insensitive unique index on `name` |
| Two people open the same conversation at once | Unique `participantKey`; both get the same thread |
| A message is retried after a network error | Unique `senderId` + `clientMessageId`; the retry returns the original |
| Messages saved out of order | `lastMessage` only moves forward in time, using an atomic update |
| Two registrations with the same email | Unique index; the second gets 409 with the right message |

## Status codes

| Code | Meaning                                                            |
| ---- | ------------------------------------------------------------------ |
| 400  | Invalid input, or an application that is incomplete when submitted |
| 401  | Missing, invalid or expired token, or wrong login details          |
| 403  | The wrong role for this endpoint, or a banned account at login     |
| 404  | Not found                                                          |
| 409  | Duplicate data, an action not allowed in the current status, or a concurrent change |
| 429  | Too many login or register attempts                                |
