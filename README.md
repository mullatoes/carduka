<div align="center">

# Carduka

**A production-shaped REST API for managing a car catalog — built on the bleeding edge of the Java + Spring ecosystem and packaged for the cloud.**

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-multi--stage-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-ready-326CE5?logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-runtime-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![H2](https://img.shields.io/badge/H2-in--memory-1f425f)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](#license)

</div>

---

## Overview

**Carduka** (Swahili: *"car duka"* — *the car shop*) is a clean, layered Spring Boot service that exposes a CRUD REST API over a `Car` resource. It is small on purpose — every line is readable — but it is built the way real production services are built: validated DTOs, RFC 7807 error responses, container-aware JVM tuning, graceful shutdown, Kubernetes-ready probes, a non-root multi-stage container image, and dual database support (H2 for development, PostgreSQL for production).

Think of it as a reference template for **how a modern Spring Boot service should be packaged in 2026**.

---

## Highlights

| | |
|---|---|
| **Modern Java** | Built on **Java 25** with **records** for immutable DTOs and **Jakarta Bean Validation** annotations |
| **Spring Boot 4.0.6** | The latest generation of Spring Boot, including `spring-boot-starter-webmvc`, Spring Data JPA, Actuator, and Validation |
| **RFC 7807 Errors** | Standardized `application/problem+json` error responses via Spring's `ProblemDetail`, surfaced through a global `@RestControllerAdvice` |
| **Dual Database** | H2 in-memory for instant local dev; PostgreSQL driver bundled for production deployment |
| **Container-aware** | JVM tuned with `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` so it respects Kubernetes pod limits |
| **Secure by default** | Container runs as a dedicated **non-root** `carduka` user |
| **Multi-stage build** | Maven build stage + slim `eclipse-temurin:25-jre` runtime stage — small, fast, reproducible |
| **K8s-ready** | Liveness/readiness probe endpoints exposed via Actuator; sample Deployment + NodePort Service manifests included |
| **Graceful shutdown** | 30s shutdown phase so in-flight requests complete before the pod terminates |
| **Lombok** | Boilerplate-free entities via `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor` |

---

## Architecture

```mermaid
flowchart LR
    Client([HTTP Client]) -->|JSON| Controller["CarController<br/>@RestController"]
    Controller -->|@Valid CarRequest| Service["CarServiceImpl<br/>@Service"]
    Service -->|JpaRepository| Repository["CarRepository<br/>extends JpaRepository"]
    Repository -->|JPA / Hibernate| DB[(H2 or PostgreSQL)]
    Controller -.error.-> Handler["GlobalExceptionHandler<br/>@RestControllerAdvice"]
    Handler -->|RFC 7807 ProblemDetail| Client
    Actuator["Actuator<br/>/health /info"] -.K8s probes.-> K8s([Kubernetes])
```

A textbook **Controller → Service → Repository** layering. The controller speaks DTOs, never entities. The service maps between the two. The repository is a single line of Spring Data magic. Errors flow through one centralized advice and come out as RFC 7807 problem documents.

### Project Layout

```
src/main/java/com/carduka
├── CardukaApplication.java         # Spring Boot entry point
├── controller/CarController.java   # REST endpoints, /api/v1/cars
├── service/
│   ├── CarService.java             # Service interface
│   └── CarServiceImpl.java         # Business logic + DTO mapping
├── repository/CarRepository.java   # Spring Data JPA
├── entity/Car.java                 # JPA @Entity (Lombok-powered)
├── dto/
│   ├── CarRequest.java             # Inbound record + validation
│   └── CarResponse.java            # Outbound record
└── exception/
    ├── CarNotFoundException.java
    └── GlobalExceptionHandler.java # @RestControllerAdvice → ProblemDetail
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 25 |
| **Framework** | Spring Boot 4.0.6 (Web MVC, Data JPA, Validation, Actuator) |
| **Persistence** | Spring Data JPA + Hibernate |
| **Database** | H2 (dev, in-memory) · PostgreSQL (prod, runtime driver) |
| **Validation** | Jakarta Bean Validation (`jakarta.validation.constraints`) |
| **Error Model** | RFC 7807 `ProblemDetail` (`application/problem+json`) |
| **Build** | Apache Maven 3.9 (wrapper included) |
| **Boilerplate** | Project Lombok (annotation-processed) |
| **Container** | Docker, multi-stage, `eclipse-temurin:25-jre` |
| **Orchestration** | Kubernetes (Deployment + NodePort Service) |
| **Observability** | Spring Boot Actuator with Kubernetes probes |

---

## API Reference

Base path: `http://localhost:8080/api/v1/cars`

| Method | Path | Status | Description |
|---|---|---|---|
| `POST` | `/api/v1/cars` | `201 Created` | Create a new car |
| `GET` | `/api/v1/cars` | `200 OK` | List all cars |
| `GET` | `/api/v1/cars/{id}` | `200 OK` | Fetch a single car by id |
| `PUT` | `/api/v1/cars/{id}` | `200 OK` | Replace an existing car |
| `DELETE` | `/api/v1/cars/{id}` | `204 No Content` | Delete a car |

### Request / Response Shape

```jsonc
// CarRequest
{
  "make": "Toyota",
  "model": "Land Cruiser",
  "manufactureYear": 2024,
  "color": "Pearl White",
  "price": 78500.00
}

// CarResponse
{
  "id": 1,
  "make": "Toyota",
  "model": "Land Cruiser",
  "manufactureYear": 2024,
  "color": "Pearl White",
  "price": 78500.00
}
```

### Validation Rules

| Field | Rule |
|---|---|
| `make` | required, non-blank |
| `model` | required, non-blank |
| `manufactureYear` | required, between **1900** and **2100** |
| `color` | required, non-blank |
| `price` | required, **strictly greater than 0** |

### Error Responses (RFC 7807)

A missing car returns a clean, standards-compliant problem document:

```http
HTTP/1.1 404 Not Found
Content-Type: application/problem+json

{
  "type": "about:blank",
  "title": "Car not found",
  "status": 404,
  "detail": "Could not find car with id 42"
}
```

---

## Quick Start

### Prerequisites

- **JDK 25**
- **Docker** (optional, for containerized run)
- **kubectl** + a local cluster like Docker Desktop / minikube / kind (optional, for K8s)

### Run locally with the Maven wrapper

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` with an in-memory H2 database. The H2 web console is enabled at `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:cardukadb`, user `sa`, no password.

### Try it with curl

```bash
# Create
curl -X POST http://localhost:8080/api/v1/cars \
  -H "Content-Type: application/json" \
  -d '{"make":"Toyota","model":"Land Cruiser","manufactureYear":2024,"color":"Pearl White","price":78500.00}'

# List
curl http://localhost:8080/api/v1/cars

# Get one
curl http://localhost:8080/api/v1/cars/1

# Update
curl -X PUT http://localhost:8080/api/v1/cars/1 \
  -H "Content-Type: application/json" \
  -d '{"make":"Toyota","model":"Land Cruiser 300","manufactureYear":2025,"color":"Pearl White","price":85000.00}'

# Delete
curl -X DELETE http://localhost:8080/api/v1/cars/1
```

### Health & Info

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
```

Liveness/readiness probes are enabled and exposed at `/actuator/health/liveness` and `/actuator/health/readiness` — wired up in `application.yaml` for Kubernetes.

---

## Build & Package

```bash
# Build a fat jar
./mvnw clean package

# Run it directly
java -jar target/carduka-0.0.1-SNAPSHOT.jar
```

---

## Docker

The included `Dockerfile` is a **multi-stage build**: a Maven stage compiles the project, then the artifact is copied into a slim `eclipse-temurin:25-jre` runtime image and executed as a non-root user.

```bash
# Build the image
docker build -t carduka:1.0.2 .

# Run the container
docker run --rm -p 8080:8080 carduka:1.0.2
```

Highlights of the image:

- **Multi-stage** — build tooling never ships to production.
- **Non-root user** (`carduka`) — defense-in-depth.
- **Container-aware JVM** — `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` honors cgroup memory limits.
- **`exec` form entrypoint** — the JVM is PID 1 and receives signals correctly for graceful shutdown.

---

## Kubernetes

Sample manifests live in [`k8s/`](k8s/):

```bash
# Apply
kubectl apply -f k8s/

# Inspect
kubectl get pods -l app=carduka
kubectl get svc carduka-service

# Reach the service (NodePort)
curl http://localhost:30080/actuator/health
```

What you get out of the box:

- **`Deployment`** with **2 replicas** for availability.
- **`Service`** of type **NodePort** (`30080`) for easy local exposure.
- **Graceful shutdown** (30s) so rolling updates do not drop in-flight requests.
- **Actuator probe endpoints** ready to wire into `livenessProbe` / `readinessProbe`.

---

## Configuration

Key configuration lives in [`src/main/resources/application.yaml`](src/main/resources/application.yaml):

| Property | Default | Purpose |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:mem:cardukadb` | Datasource (override for PostgreSQL) |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema management |
| `spring.jpa.open-in-view` | `false` | Avoid the OSIV anti-pattern |
| `spring.lifecycle.timeout-per-shutdown-phase` | `30s` | Graceful shutdown window |
| `server.shutdown` | `graceful` | Drain in-flight requests on SIGTERM |
| `management.endpoints.web.exposure.include` | `health,info` | Actuator exposure |
| `management.endpoint.health.probes.enabled` | `true` | Kubernetes liveness/readiness probes |

Switch to PostgreSQL by overriding the datasource via environment variables — the driver is already on the classpath:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/carduka \
SPRING_DATASOURCE_USERNAME=carduka \
SPRING_DATASOURCE_PASSWORD=secret \
java -jar target/carduka-0.0.1-SNAPSHOT.jar
```

---

## Engineering Choices Worth Noting

- **Records for DTOs.** `CarRequest` and `CarResponse` are immutable Java records — concise, thread-safe, and naturally JSON-friendly.
- **Lombok only on the entity layer.** The service uses `@RequiredArgsConstructor` for clean constructor injection (no field injection, no `@Autowired` clutter).
- **`open-in-view: false`.** Disables Spring's default Open Session In View pattern, surfacing lazy-loading mistakes early instead of hiding them behind the request thread.
- **`ProblemDetail`-based errors.** Standardized, content-typed (`application/problem+json`), and self-describing — clients never have to guess your error schema.
- **Constructor injection everywhere.** Dependencies are `final` and injected via constructors — testable, immutable, and Spring-idiomatic.
- **No password in dev.** The H2 datasource uses the conventional empty password; production is expected to inject credentials via environment variables.

---

## Roadmap

Pragmatic next steps for taking this from a polished template to a production service:

- [ ] OpenAPI / Swagger UI via `springdoc-openapi`
- [ ] Pagination + filtering on `GET /api/v1/cars`
- [ ] Spring Security with JWT-based authentication
- [ ] Flyway or Liquibase for versioned schema migrations
- [ ] Testcontainers-backed integration tests against real PostgreSQL
- [ ] GitHub Actions CI (build, test, push image, scan)
- [ ] Structured JSON logging + Micrometer + Prometheus metrics
- [ ] Helm chart in addition to the raw manifests

---

## License

Released under the MIT License. See [`LICENSE`](LICENSE) for details.

---

<div align="center">

**Built with care, packaged for production.**

</div>
