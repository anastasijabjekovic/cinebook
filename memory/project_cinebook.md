---
name: CineBook project overview
description: CineBook is a Spring Boot microservices cinema booking platform — structure, tech stack, and module ports
type: project
---

CineBook is a Maven multi-module Spring Boot 3.3.0 microservices application.

**Why:** Cinema reservation platform being built from scratch.

**How to apply:** Use this context when suggesting new features, modules, or changes.

## Module structure

| Module | Port | Package | Description |
|---|---|---|---|
| api-gateway | 8080 | com.cinebook.gateway | Spring Cloud Gateway, routes all traffic |
| user-service | 8081 | com.cinebook.user | Registration, auth, profiles (PostgreSQL + Security) |
| movie-service | 8082 | com.cinebook.movie | Movies, screenings, halls (PostgreSQL + Redis cache) |
| booking-service | 8083 | com.cinebook.booking | Reservations, payments (PostgreSQL + Kafka + Feign) |
| notification-service | 8084 | com.cinebook.notification | Email notifications (Kafka consumer + Thymeleaf + Mail) |

## Tech stack
- Java 21, Spring Boot 3.3.0, Spring Cloud 2023.0.1
- PostgreSQL per service (separate databases)
- Redis for movie-service caching
- Kafka for async messaging (booking -> notification)
- Eureka for service discovery
- Spring Cloud Gateway (reactive)
- Feign for inter-service HTTP calls (booking-service)
- Lombok, Spring Validation, Actuator everywhere

## groupId / artifactId
- groupId: com.cinebook
- root artifactId: cinebook
- version: 1.0.0-SNAPSHOT
