# Demo data

`seed-service` runs once when you start Docker Compose. It adds the data below, and skips it if the data already exists.

To restore the demo data to its original state:

```bash
SEED_RESET=true docker compose up --build seed-service
```

The password for every account is `Demo@12345`. These are public test accounts, so don't use this seed on a real production database.

To add sample projects to your own client accounts, list their emails. Existing sample data isn't duplicated:

```bash
SEED_REAL_ACCOUNTS=you@example.com docker compose up --build seed-service
```

## Accounts

All emails end in `@lanceify.demo`.

| Email | Role | Scenario |
| --- | --- | --- |
| `admin` | Admin | Reviews applications, approves proposals, bans users |
| `client.omar` | Client | Has open, active and completed projects, and chats |
| `client.layla` | Client | Has a full project, near-limit project and cancelled work |
| `client.new` | Client | Has nothing yet |
| `client.banned` | Client | Banned; login is refused |
| `freelancer.sara` | Freelancer | Approved with an active contract and an unread chat |
| `freelancer.yousif` | Freelancer | Approved with a completed contract |
| `freelancer.noor` | Freelancer | Approved, busy, with a cancelled contract |
| `freelancer.maryam` | Freelancer | Approved designer in a full project |
| `freelancer.hamad` | Freelancer | Approved with pending and rejected proposals |
| `freelancer.busy` | Freelancer | Approved with the maximum 3 active contracts |
| `freelancer.spare` | Freelancer | Approved with no proposals; use to test the limit |
| `freelancer.noprofile` | Freelancer | Has no application yet |
| `freelancer.draft` | Freelancer | Application is a draft |
| `freelancer.pending` | Freelancer | Application is waiting for review |
| `freelancer.rejected` | Freelancer | Application was rejected with a reason |
| `freelancer.banned` | Freelancer | Banned; login is refused |

## Projects

| Project | Status | What to try |
| --- | --- | --- |
| Company website | Open, no proposals | Send the first proposal |
| Booking system | Open, mixed proposals | Withdrawn and rejected proposals don't count toward the limit |
| Online store | Open, 4 of 5 proposals | One more proposal fits |
| Mobile landing page | Open, 5 of 5 proposals | As `freelancer.spare`, sending one returns 409 |
| Capacity test project | Open | As `client.omar`, accepting `freelancer.busy` returns 409 |
| Inventory dashboard | In progress | Active contract; complete it as `client.omar` |
| Portfolio site | Completed | Completed contract |
| Logo refresh | Cancelled | Cancelled contract |
| Cancelled idea | Cancelled | No contract |
| Capacity project 1 to 3 | In progress | Fill `freelancer.busy`'s contract limit |

## Chats

| Between | Scenario |
| --- | --- |
| Omar and Sara | Sara has one unread message |
| Omar and Yousif | Everything read |
| Layla and Maryam | No messages yet |
