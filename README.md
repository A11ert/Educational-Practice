# Marketplace API

A backend-only marketplace where items can be listed for sale, added to a basket,
and marked as favorites. Built with Spring Boot, Spring Web, Spring Data JPA and
Bean Validation. The API is stateless and returns JSON.

## Tech stack

- Java 17, Spring Boot 4.1
- Spring Web (REST controllers)
- Spring Data JPA + Hibernate (persistence)
- Bean Validation (request validation)
- PostgreSQL at runtime, H2 in-memory for the test suite

## Running

The application connects to a PostgreSQL database named `EducationalPractice` on
`localhost:5432`. Create it first if it does not exist:

```sql
CREATE DATABASE "EducationalPractice";
```

Credentials are read from the `DB_USERNAME` and `DB_PASSWORD` environment variables
(falling back to `postgres` / `postgres`). Set them to match your database, then run:

```bash
# PowerShell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-password"
./gradlew bootRun
```

The application starts on `http://localhost:8080`. Hibernate creates the tables
automatically on first start (`spring.jpa.hibernate.ddl-auto=update`).

Run the tests with:

```bash
./gradlew test
```

## Architecture

The code follows a layered structure and is organised by feature:

```
common/   shared paged response and centralised error handling
item/     item listings, search, and favorites
basket/   the basket and its contents
```

Each feature has the same layering: **controller → service → repository**.
Controllers only accept and return DTOs (`record` types); JPA entities are never
exposed directly. Services hold the business logic and are defined behind an
interface so callers depend on an abstraction rather than a concrete class.

### Design decisions

- **No authentication.** User accounts were out of scope, so the basket and
  favorites are global rather than per-user. The favorites collection is every
  item whose `favorite` flag is set, and there is a single global basket. With
  authentication added later, each user would simply own their own basket.
- **The basket references items by id**, not through a JPA relationship. The
  basket and the item are separate aggregates, so keeping the reference loose
  means deleting an item never leaves the basket in a broken state — a removed
  item just stops appearing in the basket contents.
- **Validation and errors are centralised.** Incoming DTOs are validated with
  `@Valid`, and a single `@RestControllerAdvice` translates validation failures,
  missing resources and bad input into consistent JSON error responses.

## Error response shape

Every handled error returns the same structure (validation errors also include a
`fieldErrors` array):

```json
{
  "timestamp": "2026-06-17T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/items",
  "fieldErrors": [
    { "field": "title", "message": "must not be blank" }
  ]
}
```

## Endpoints

Base path: `/api`

### Items

| Method | Path           | Description                          | Success |
|--------|----------------|--------------------------------------|---------|
| POST   | `/items`       | Create a new item listing            | 201     |
| GET    | `/items/{id}`  | Get a single item by id              | 200     |
| GET    | `/items`       | List / search items (paginated)      | 200     |
| PUT    | `/items/{id}`  | Update an existing item              | 200     |
| DELETE | `/items/{id}`  | Delete an item                       | 204     |

**Item request body** (used for create and update):

```json
{
  "title": "Road bike",
  "description": "Lightweight aluminium frame",
  "price": 499.99,
  "tags": ["sport", "outdoor"]
}
```

`title` is required (max 120 chars), `price` is required and must be zero or
greater, `description` and `tags` are optional.

**Item response body:**

```json
{
  "id": 1,
  "title": "Road bike",
  "description": "Lightweight aluminium frame",
  "price": 499.99,
  "tags": ["sport", "outdoor"],
  "favorite": false,
  "createdAt": "2026-06-17T10:15:30Z",
  "updatedAt": "2026-06-17T10:15:30Z"
}
```

**Search / filter** — all parameters are optional and can be combined:

| Parameter  | Example            | Meaning                                  |
|------------|--------------------|------------------------------------------|
| `keyword`  | `bike`             | Matches title or description             |
| `tags`     | `tags=sport&tags=outdoor` | Items having any of the given tags |
| `minPrice` | `100`              | Minimum price (inclusive)                |
| `maxPrice` | `500`              | Maximum price (inclusive)                |
| `page`     | `0`                | Page number (default 0)                  |
| `size`     | `20`               | Page size (default 20)                   |
| `sort`     | `price,asc`        | Sort field and direction (default `createdAt,desc`) |

Example: `GET /api/items?keyword=bike&minPrice=100&maxPrice=500&page=0&size=10`

**Paged response body:**

```json
{
  "content": [ /* item responses */ ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "last": false
}
```

### Favorites

| Method | Path                    | Description                  | Success |
|--------|-------------------------|------------------------------|---------|
| PUT    | `/items/{id}/favorite`  | Mark an item as favorite     | 200     |
| DELETE | `/items/{id}/favorite`  | Remove an item from favorites| 200     |
| GET    | `/favorites`            | List all favorited items     | 200     |

The two `/items/{id}/favorite` endpoints return the updated item; `/favorites`
returns an array of item responses.

### Basket

| Method | Path                    | Description                       | Success |
|--------|-------------------------|-----------------------------------|---------|
| GET    | `/basket`               | View current basket contents      | 200     |
| POST   | `/basket/items/{itemId}`| Add an item to the basket         | 200     |
| DELETE | `/basket/items/{itemId}`| Remove an item from the basket    | 200     |

**Basket response body:**

```json
{
  "id": 1,
  "items": [ /* item responses */ ],
  "itemCount": 2,
  "totalPrice": 519.99
}
```

## Database configuration

The schema is managed by Hibernate (`spring.jpa.hibernate.ddl-auto=update`), so no
manual SQL is needed beyond creating the database. Runtime settings live in
`src/main/resources/application.properties`; the test suite overrides them with H2
in `src/test/resources/application.properties`, so tests need no running database.

To point the application at a different server (or switch to MySQL), change the
driver dependency in `build.gradle` and the `spring.datasource.*` properties. The
entity mappings and the rest of the code stay the same.
```
