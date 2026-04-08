# vertx-todo-api

A reactive CRUD REST API for managing todos, built with Eclipse Vert.x 5 and MySQL. Demonstrates non-blocking async
database interactions using the Vert.x MySQL client, event-driven Verticle architecture, and RESTful routing with Vert.x
Web.

---

## Tech Stack

| Technology          | Version | Purpose               |
|---------------------|---------|-----------------------|
| Java                | 21+     | Primary language      |
| Eclipse Vert.x      | 5.0.10  | Reactive framework    |
| Vert.x Web          | 5.0.10  | HTTP routing          |
| Vert.x MySQL Client | 5.0.10  | Async DB interactions |
| MySQL / MariaDB     | 8.0+    | Database              |
| Logback             | 1.5.x   | Logging               |
| Maven               | 3.9+    | Build tool            |

---

## Project Structure

```
src/main/java/dev/saivinay/Todo_Project/
├── MainVerticle.java          # Entry point — deploys all verticles in order
├── HttpVerticle.java          # HTTP server and route definitions
├── DatabaseVerticle.java      # MySQL connection pool management
├── TodoHandler.java           # CRUD business logic
└── Todo.java                  # Todo model
```

---

## Architecture

```
Client Request
      │
      ▼
 HttpVerticle          ← handles routing (port 8080)
      │
      ▼
 TodoHandler           ← business logic + validation
      │
      ▼
 DatabaseVerticle      ← async MySQL connection pool
      │
      ▼
   MySQL DB            ← persists todo data
```

### Key Vert.x concepts used

- **Verticles** — each layer is a separate deployable unit
- **Async non-blocking** — all DB calls use `Future` / `onSuccess` / `onFailure`
- **Promise** — used in `start(Promise)` to signal deployment readiness
- **Connection Pool** — managed by `DatabaseVerticle`, shared via dependency injection

---

## Prerequisites

- Java 21 or higher
- Maven 3.9+
- MySQL or MariaDB running locally

---

## Database Setup

Connect to your MySQL client and run:

```sql
CREATE DATABASE tododb;

USE tododb;

CREATE TABLE todos
(
  id         VARCHAR(36) PRIMARY KEY,
  title      VARCHAR(255) NOT NULL,
  completed  BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP             DEFAULT NOW()
);
```

Optional — insert sample data:

```sql
INSERT INTO todos (id, title, completed)
VALUES (UUID(), 'Buy groceries', false),
       (UUID(), 'Read a book', true),
       (UUID(), 'Learn Vert.x', false),
       (UUID(), 'Build REST API', true),
       (UUID(), 'Write unit tests', false),
       (UUID(), 'Setup Docker', false),
       (UUID(), 'Deploy to AWS', false),
       (UUID(), 'Learn Kafka', false),
       (UUID(), 'Fix production bug', true),
       (UUID(), 'Code review PR', false);
```

---

## Configuration

Update the database credentials in `DatabaseVerticle.java`:

```java
MySQLConnectOptions connectOptions = new MySQLConnectOptions()
  .setHost("localhost")
  .setPort(3306)
  .setDatabase("tododb")
  .setUser("your-username")
  .setPassword("your-password");
```

---

## Running the Application

```bash
# clone the repository
git clone https://github.com/saivinay/vertx-todo-api.git
cd vertx-todo-api

# build and run
mvn clean compile exec:java
```

You should see:

```
MySQL connected successfully!
DatabaseVerticle deployed
Todo API running on http://localhost:8080
HttpVerticle deployed
```

---

## API Reference

### Get all todos

```
GET /api/todos
```

Response `200 OK`:

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Learn Vert.x",
    "completed": false
  }
]
```

---

### Get todo by ID

```
GET /api/todos/:id
```

Response `200 OK`:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Learn Vert.x",
  "completed": false
}
```

Response `404 Not Found`:

```json
{
  "error": "Todo not found"
}
```

---

### Create a todo

```
POST /api/todos
Content-Type: application/json

{
  "title": "Learn Kafka"
}
```

Response `201 Created`:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Learn Kafka",
  "completed": false
}
```

Response `400 Bad Request`:

```json
{
  "error": "Title is mandatory"
}
```

---

### Update a todo

```
PUT /api/todos/:id
Content-Type: application/json

{
  "title": "Updated title",
  "completed": true
}
```

Response `200 OK`:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Updated title",
  "completed": true
}
```

---

### Delete a todo

```
DELETE /api/todos/:id
```

Response `204 No Content`

Response `404 Not Found`:

```json
{
  "error": "Todo not found"
}
```

---

## Testing with curl

```bash
# get all todos
curl http://localhost:8080/api/todos

# get by id
curl http://localhost:8080/api/todos/{id}

# create
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn Kafka"}'

# update
curl -X PUT http://localhost:8080/api/todos/{id} \
  -H "Content-Type: application/json" \
  -d '{"completed": true}'

# delete
curl -X DELETE http://localhost:8080/api/todos/{id}
```

---

## What I Learned

- Setting up a Vert.x project with Maven
- Verticle lifecycle — `start()`, `stop()`, `Promise`
- Non-blocking async patterns with `Future`, `compose()`, `onSuccess()`, `onFailure()`
- Routing HTTP requests with Vert.x Web
- Async MySQL interactions with the Vert.x reactive MySQL client
- Prepared statements and parameterized queries
- Separation of concerns — routing, business logic, and DB in separate Verticles
- Production patterns — connection pooling, graceful shutdown, error handling

---

## License

MIT
