# Psylog API

Spring Boot backend for a clinical psychologist portfolio and article publishing platform.

## Tech Stack

- Java / Spring Boot
- PostgreSQL
- JPA / Hibernate
- JWT Authentication

## Features

- Public portfolio pages
- Article listing and detail
- Contact form
- Admin login
- Post CRUD (create, publish, unpublish, delete)
- About section management

## Getting Started

1. Create a PostgreSQL database named `psylog`
2. Copy the example config and fill in your values:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

3. Run the application:

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.
