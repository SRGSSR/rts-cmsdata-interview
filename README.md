# Interview

Welcome to this interview session!

## Prerequisites

- Java 17
- Maven
- Docker and Docker Compose

## Quickstart

### Database

```bash
cd docker
docker compose up -d
```

You can connect with password `interview`
```bash
docker exec -it interview-postgres psql -U interview -d interview
``` 

### Application

Run the application with the local profile to connect to the PostgreSQL database:

```bash
./mvnw spring-boot:run
```

Your application is served:
 * `http://localhost:8080/`
 * Swagger: `http://localhost:8080/swagger-ui/index.html`

### Tasks

Read TODO.md to get the tasks. :) 
