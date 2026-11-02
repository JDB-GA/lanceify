# Database design

Each service owns its own MongoDB database, so these relationships are stored as ids, not database foreign keys. Fields ending in `Ids` are arrays that hold many-to-many relationships.

| Database | Collections | Service |
| --- | --- | --- |
| `identity_db` | `users`, `freelancer_profiles` | `identity-service` |
| `catalog_db` | `skills`, `categories` | `catalog-service` |
| `project_db` | `projects` | `project-service` |
| `proposal_db` | `proposals` | `proposal-service` |
| `contract_db` | `contracts` | `contract-service` |
| `communication_db` | `conversations`, `messages` | `communication-service` |

```mermaid
erDiagram
  USERS ||--o| FREELANCER_PROFILES : "freelancer has"
  USERS ||--o{ PROJECTS : "client posts"
  USERS |o--o{ PROJECTS : "freelancer assigned to"
  USERS ||--o{ PROPOSALS : "freelancer sends"
  USERS ||--o{ PROPOSALS : "client receives"
  USERS ||--o{ CONTRACTS : "client signs"
  USERS ||--o{ CONTRACTS : "freelancer signs"
  USERS }o--o{ CONVERSATIONS : "participates in"
  USERS ||--o{ MESSAGES : sends
  USERS ||--o{ MESSAGES : receives
  SKILLS }o--o{ FREELANCER_PROFILES : "assigned to"
  SKILLS }o--o{ CATEGORIES : "grouped in"
  SKILLS }o--o{ PROJECTS : "required by"
  CATEGORIES ||--o{ PROJECTS : classifies
  PROJECTS ||--o{ PROPOSALS : "receives up to 5 active"
  PROPOSALS ||--o| CONTRACTS : "becomes when approved"
  PROJECTS ||--o| CONTRACTS : "has active"
  PROJECTS |o--o{ CONVERSATIONS : "discussed in"
  CONVERSATIONS ||--o{ MESSAGES : contains

  USERS {
    string id PK
    string firstName
    string lastName
    string username UK
    string email UK
    string password "BCrypt hash"
    enum role "CLIENT, FREELANCER, ADMIN"
    boolean banned
    instant createdAt
    instant updatedAt
  }
  FREELANCER_PROFILES {
    string id PK
    string userId FK,UK
    string bio
    decimal minPrice
    decimal maxPrice
    enum availability "AVAILABLE, BUSY"
    array skillIds FK
    enum approvalStatus "DRAFT, PENDING, APPROVED, REJECTED"
    string rejectionReason
    instant submittedAt
    instant reviewedAt
    long version
  }
  SKILLS {
    string id PK
    string name UK
    boolean active
    long version
  }
  CATEGORIES {
    string id PK
    string name UK
    string description
    array skillIds FK
    boolean active
    long version
  }
  PROJECTS {
    string id PK
    string clientId FK
    string assignedFreelancerId FK
    string title
    string description
    decimal budget
    date deadline
    string categoryId FK
    array requiredSkillIds FK
    enum status "OPEN, IN_PROGRESS, COMPLETED, CANCELLED"
    datetime createdAt
  }
  PROPOSALS {
    string id PK
    string projectId FK
    string clientId FK
    string freelancerId FK
    string projectTitle
    string coverLetter
    decimal bidAmount
    int deliveryDays
    enum status "PENDING, ACCEPTED, REJECTED, WITHDRAWN"
    instant decidedAt
    instant createdAt
    instant updatedAt
    long version
  }
  CONTRACTS {
    string id PK
    string proposalId FK,UK
    string projectId FK
    string clientId FK
    string freelancerId FK
    string projectTitle
    decimal amount
    int deliveryDays
    enum status "ACTIVE, COMPLETED, CANCELLED"
    instant createdAt
    instant closedAt
  }
  CONVERSATIONS {
    string id PK
    array participantIds FK
    string participantKey
    string projectId FK
    string lastMessage
    string lastSenderId FK
    instant lastMessageAt
    instant createdAt
  }
  MESSAGES {
    string id PK
    string conversationId FK
    string senderId FK
    string recipientId FK
    string content
    string clientMessageId
    boolean read
    instant createdAt
  }
```

## Rules the design enforces

| Rule | How |
| --- | --- |
| One profile per freelancer | Unique `userId` |
| One proposal per freelancer on a project | Unique index on `projectId` and `freelancerId` |
| One accepted proposal per project | Partial unique index on `projectId` where status is `ACCEPTED` |
| At most 5 active proposals per project | `ReentrantLock` per project in `proposal-service` |
| One contract per proposal | Unique `proposalId` |
| At most 3 active contracts per freelancer | `ReentrantLock` per freelancer in `contract-service` |
