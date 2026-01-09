# API Aggregator

Production-ready Spring Boot REST API that aggregates contact information from external APIs with automatic pagination handling.

## Features

- REST endpoint with pagination support for retrieving contacts
- Automatic external API pagination handling (RFC8288 standard)
- Response pagination with configurable page size
- Source filtering (extensible for multiple sources)
- Bean Validation (Jakarta Validation) for input parameters
- In-memory caching to reduce external API calls
- Comprehensive error handling with detailed validation messages
- Full test coverage (unit + integration tests)
- Clean architecture with separation of concerns
- Production-ready configuration

## Tech Stack

- Java 17
- Spring Boot 3.2.1
- Spring WebFlux (WebClient)
- Spring Cache + Caffeine
- Lombok
- JUnit 5 + Mockito
- MockWebServer

## API Documentation

### GET /contacts

Returns contacts from external sources. Response format depends on whether pagination parameters are provided.

**Query Parameters:**
- `page` (optional) - Page number (validated: must be ≥ 1)
- `size` (optional) - Items per page (validated: must be between 1-100)
- `source` (optional) - Filter by contact source (enum: KENECT_LABS)

#### Without Pagination Parameters

Returns **all contacts** in a flat array.

**Examples:**
```bash
GET /contacts
GET /contacts?source=KENECT_LABS
```

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
  },
  {
    "id": 2,
    "name": "John Doe",
    "email": "john@example.net",
    "source": "KENECT_LABS",
    "createdAt": "2020-06-24T19:37:16.688Z",
    "updatedAt": "2020-06-24T19:37:16.688Z"
  }
]
```

#### With Pagination Parameters

Returns **paginated response** with metadata.

**Examples:**
```bash
GET /contacts?page=1
GET /contacts?size=10
GET /contacts?page=2&size=10
GET /contacts?page=1&source=KENECT_LABS
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

**Response Headers (Paginated only):**
- `X-Total-Count` - Total number of contacts
- `X-Total-Pages` - Total number of pages
- `X-Current-Page` - Current page number

**Status Codes:**
- `200 OK` - Successfully retrieved contacts
- `400 Bad Request` - Invalid request parameters (validation failure)
- `502 Bad Gateway` - External API unavailable
- `500 Internal Server Error` - Unexpected error

## Input Validation

The API implements Bean Validation (Jakarta Validation) for request parameters. Validation rules are defined in the `ContactQueryParams` DTO, not in the controller layer.

**Validation Rules:**
- `page`: Must be greater than or equal to 1
- `size`: Must be between 1 and 100
- `source`: Must be a valid ContactSource enum value (KENECT_LABS)

**Validation Error Response:**
```json
{
  "timestamp": "2026-01-09T15:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "details": "Page number must be greater than 0"
}
```

**Example Validation Failures:**
```bash
GET /contacts?page=0           # Error: Page number must be greater than 0
GET /contacts?size=150         # Error: Page size must not exceed 100
GET /contacts?source=INVALID   # Error: Invalid enum value
```

## Building and Running

### Prerequisites
- Java 17+
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

## Caching

The application uses Caffeine for in-memory caching to minimize external API calls and improve performance.

**Cache Configuration:**
- **Enabled**: Configurable via `cache.enabled` (default: `true`)
- **TTL**: 5 minutes
- **Max Size**: 100 entries
- **Strategy**: Cache-aside pattern

**Toggle Caching:**
```yaml
cache:
  enabled: true  # Set to false to disable caching
```

**Behavior:**
- First request fetches from external API and caches result
- Subsequent requests within 5 minutes return cached data
- Cache automatically expires after TTL
- Cache can be manually evicted if needed
- Disable caching for debugging or testing environments

**Performance Impact:**
- Reduces external API load
- Improves response time for repeated queries
- Maintains data freshness with TTL

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

- **Unit Tests**: Mapper, Service, Controller, Client, Cache
- **Integration Tests**: Full application flow
- **Coverage**: All critical paths and edge cases
