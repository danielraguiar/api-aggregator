# API Aggregator

Production-ready Spring Boot REST API that aggregates contact information from external APIs with automatic pagination handling.

## Features

- REST endpoint with pagination support for retrieving contacts
- Automatic external API pagination handling (RFC8288 standard)
- Response pagination with configurable page size
- Comprehensive error handling and validation
- Full test coverage (unit + integration tests)
- Clean architecture with separation of concerns
- Production-ready configuration

## Tech Stack

- Java 17
- Spring Boot 3.2.1
- Spring WebFlux (WebClient)
- Lombok
- JUnit 5 + Mockito
- MockWebServer

## API Documentation

### GET /contacts

Returns paginated contacts from external sources.

**Query Parameters:**
- `page` (optional, default: 1) - Page number (must be > 0)
- `size` (optional, default: 20) - Items per page (1-100)

**Example Requests:**
```bash
GET /contacts
GET /contacts?page=2
GET /contacts?page=2&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Mrs. Willian Bradtke",
      "email": "jerold@example.net",
      "source": "KENECT_LABS",
      "createdAt": "2020-06-24T19:37:16.688Z",
      "updatedAt": "2020-06-24T19:37:16.688Z"
    }
  ],
  "page": 1,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

**Response Headers:**
- `X-Total-Count` - Total number of contacts
- `X-Total-Pages` - Total number of pages
- `X-Current-Page` - Current page number

**Status Codes:**
- `200 OK` - Successfully retrieved contacts
- `400 Bad Request` - Invalid pagination parameters
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
