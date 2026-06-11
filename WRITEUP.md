# URL Shortener – Engineering Reflection

## 1. What I asked the AI to do, and what I wrote or decided myself

I used AI primarily as an implementation accelerator rather than as an architect. I asked it to generate an initial Spring Boot project structure, suggest a REST API design, create JPA entities and repositories, generate DTOs, and help draft automated tests. I also used it to brainstorm approaches for generating collision-free short codes and handling edge cases such as duplicate URLs, invalid URLs, and custom alias conflicts.

The core design decisions were my own. I chose to implement the service using Spring Boot and JPA with a persistent datastore because it provided a clean separation between the API, business logic, and persistence layers. I decided that shortening the same URL multiple times should be idempotent, meaning the service returns the existing short code rather than creating duplicates. I also decided that custom aliases should be unique and return an HTTP 409 Conflict if already in use. For redirects, I intentionally used HTTP 301 (Moved Permanently) because the assignment specifically required permanent redirection behavior.

For short-code generation, I selected a Base62 encoding strategy built on top of unique database-generated IDs. This guarantees uniqueness without requiring collision checks or retry loops and keeps generated codes URL-safe and compact.

---

## 2. Where I overrode, corrected, or threw away the AI output — and why

One of the first AI-generated solutions used random strings for short-code generation and relied on checking the database repeatedly until a unique code was found. While functional, I rejected that approach because it introduces collision handling logic, extra database lookups, and unnecessary complexity. I replaced it with a deterministic Base62 encoding of unique IDs, which guarantees uniqueness by construction.

The AI also suggested storing multiple short codes for the same long URL. I decided against that because it complicates behavior and creates ambiguity around what should happen when a user shortens the same URL repeatedly. Returning the existing mapping keeps the API predictable and reduces duplicate records.

Another suggestion included additional features such as expiration dates, click analytics, and user accounts. While useful in a production system, I intentionally excluded them because they were outside the scope of the exercise. My goal was to keep the implementation focused on the required functionality and produce a codebase that is easy to explain and extend during a follow-up discussion.

---

## 3. The two or three biggest trade-offs I made

### 1. Base62 IDs vs Random Short Codes

I chose Base62 encoding of unique numeric IDs instead of random string generation.

**Advantages**
- Guaranteed uniqueness.
- No collision detection required.
- Simpler implementation.
- Predictable performance.

**Disadvantages**
- Generated codes are somewhat sequential and therefore more predictable.
- Less suitable if obscurity is a requirement.

For this assignment, simplicity and correctness were more important than preventing predictability.

### 2. Idempotent URL Creation vs Multiple Aliases per URL

I decided that shortening the same URL multiple times should return the same short code.

**Advantages**
- Prevents duplicate records.
- Easier to reason about.
- Saves storage.

**Disadvantages**
- Users cannot automatically generate multiple different short URLs for the same destination unless they explicitly use custom aliases.

I preferred consistency and simplicity for the default behavior.

### 3. H2 Persistence vs Production Database

I used H2 for persistence because it keeps setup lightweight and allows the project to run immediately without external dependencies.

**Advantages**
- Easy local setup.
- Fast development and testing.
- No infrastructure requirements.

**Disadvantages**
- Not suitable for production workloads.
- Limited operational characteristics compared to PostgreSQL or MySQL.

Given the scope of the exercise, minimizing setup friction was the right trade-off.

---

## 4. What’s missing, or what I’d do with another day

If I had another day, I would focus on production-oriented improvements rather than adding more features.

The first addition would be click analytics, including tracking redirect counts and timestamps for each short URL. This would make the service more useful and create a foundation for reporting and insights.

I would also add rate limiting to protect the shortening endpoint from abuse and denial-of-service scenarios. In a public-facing service, this would be an important security measure.

Other improvements would include:

- Docker support for easier deployment.
- PostgreSQL integration instead of H2.
- Metrics and observability using Spring Actuator.
- URL normalization to better detect equivalent URLs.
- Expiring links and scheduled cleanup.
- Load and performance testing.
- More comprehensive validation and edge-case coverage.

The current implementation focuses on correctness, simplicity, and clear design decisions while leaving room for future extension.