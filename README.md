# API Aggregator

Production-ready Spring Boot REST API that aggregates contact information from external APIs with automatic pagination handling.

## Features

- REST endpoint for retrieving aggregated contacts
- Automatic pagination handling (RFC8288 standard)
- Comprehensive error handling
- Full test coverage (unit + integration tests)
- Clean architecture with separation of concerns
- Production-ready configuration

## Tech Stack

- Java 21
- Spring Boot 4.0.1
- Spring WebFlux (WebClient)
- Lombok
- JUnit 5 + Mockito
- MockWebServer

## API Documentation

### GET /contacts

Returns all contacts from external sources.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Mrs. Willian Bradtke",
    "email": "jerold@example.net",
    "source": "KENECT_LABS",
    "createdAt": "2020-06-24T19:37:16.688Z",
    "updatedAt": "2020-06-24T19:37:16.688Z"
  }
]
```

**Status Codes:**
- `200 OK` - Successfully retrieved contacts
- `502 Bad Gateway` - External API unavailable
- `500 Internal Server Error` - Unexpected error

## Building and Running

### Prerequisites
- Java 21+
- Maven 3.6+

### Build
```bash
mvnw clean install
```

### Run Tests
```bash
mvnw test
```

### Run Application
```bash
mvnw spring-boot:run
```

Application starts at `http://localhost:8080`

### Test Endpoint
```bash
curl http://localhost:8080/contacts
```

## Configuration

Configure in `application.yaml`:

```yaml
kenect:
  api:
    base-url: https://candidate-challenge-api-489237493095.us-central1.run.app
    bearer-token: your-token-here
    timeout:
      connect: 5000
      read: 30000
      write: 30000
```

## Architecture

```
controller/    REST endpoints
service/       Business logic
client/        External API integration
model/         Domain entities
dto/           Data transfer objects
mapper/        Object mapping
config/        Application configuration
exception/     Error handling
```

## Design Principles

- **SOLID**: Single Responsibility, Open/Closed, Dependency Inversion
- **Clean Architecture**: Clear separation of layers
- **DRY**: No code duplication
- **Production Ready**: Proper logging, error handling, timeouts

## Testing

- **Unit Tests**: Mapper, Service, Controller, Client
- **Integration Tests**: Full application flow
- **Coverage**: All critical paths and edge cases
