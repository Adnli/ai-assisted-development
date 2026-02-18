# Orders API (Spring Boot)

## Overview
REST API for order management with pagination, filtering, and validation support. Uses H2 in-memory database and automatically creates test data on startup.

## Requirements
- Java 21
- Gradle 8.x (wrapper included)

## Quick Start

### Build
```powershell
./gradlew.bat build
```

### Run
```powershell
./gradlew.bat bootRun
```

Application starts on `http://localhost:8080`.

### Test
```powershell
./gradlew.bat test
```

**Test Coverage:** 85% (target ≥80% ✓)

### Coverage Report
```powershell
./gradlew.bat test jacocoTestReport
```

HTML report: `build/reports/jacoco/test/html/index.html`.

## Architecture

```
src/
├── main/
│   ├── java/.../
│   │   ├── controller/     # REST endpoints
│   │   ├── service/        # Business logic + filtering
│   │   ├── repository/     # JPA repository
│   │   ├── model/
│   │   │   ├── entity/     # Order, OrderStatus
│   │   │   ├── dto/        # Request/Response DTOs
│   │   │   └── error/      # Error response models
│   │   └── config/         # Data seeder
│   └── resources/
│       └── application.yaml
└── test/
    └── java/.../
        └── OrderControllerTest.java  # 15 integration tests
```

## Database

### Configuration
- **Type:** H2 in-memory
- **Console:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:ordersdb`
- **Username:** `sa`
- **Password:** _(empty)_

### Schema
```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,  -- NEW, PAID, SHIPPED, CANCELLED
    amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Data Seeding

On application startup, **50 test orders** are automatically created:
- **25 main orders** (status alternates PAID/NEW, amount from 10.00 to 34.00)
- **20 orders with CANCELLED status** (amount from 5.00 to 24.00)
- **3 orders with boundary amounts** (50.00, 75.00, 100.00)
- **1 combined order** (NEW, 25.00)
- **1 duplicate** for stability testing

Uses deterministic seed (`424242`) for data reproducibility.

## API Endpoints

### POST /orders
Create a new order.

**Request:**
```json
{
  "customerName": "Alice Johnson",
  "status": "NEW",
  "amount": 125.50
}
```

**Validation:**
- `customerName`: required field, not blank
- `status`: one of [NEW, PAID, SHIPPED, CANCELLED]
- `amount`: positive number

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "Alice Johnson",
  "status": "NEW",
  "amount": 125.50,
  "createdAt": "2024-02-18T10:30:00Z",
  "updatedAt": "2024-02-18T10:30:00Z"
}
```

**Errors:**
- `400 Bad Request` - invalid data
- `422 Unprocessable Entity` - incorrect data type

### GET /orders
Get list of orders with pagination and filtering.

**Query Parameters:**

| Parameter | Type | Default | Range | Description |
|-----------|------|---------|-------|-------------|
| `page` | int | 1 | 1..∞ | Page number |
| `limit` | int | 10 | 1..100 | Page size |
| `status` | enum | - | NEW, PAID, SHIPPED, CANCELLED | Filter by status |
| `minAmount` | decimal | - | >0 | Minimum amount (inclusive) |
| `maxAmount` | decimal | - | >0 | Maximum amount (inclusive) |
| `fromDate` | ISO-8601 | - | - | Period start (inclusive) |
| `toDate` | ISO-8601 | - | - | Period end (inclusive) |

**Examples:**

Basic pagination:
```bash
curl "http://localhost:8080/orders?page=2&limit=15"
```

Filter by status:
```bash
curl "http://localhost:8080/orders?status=PAID"
```

Filter by amount range:
```bash
curl "http://localhost:8080/orders?minAmount=50.00&maxAmount=150.00"
```

Filter by dates (inclusive):
```bash
curl "http://localhost:8080/orders?fromDate=2024-01-10T00:00:00Z&toDate=2024-01-15T23:59:59Z"
```

Combined filters:
```bash
curl "http://localhost:8080/orders?status=NEW&minAmount=20.00&fromDate=2024-01-05T00:00:00Z&toDate=2024-01-20T00:00:00Z"
```

**Response:** `200 OK`
```json
{
  "items": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "customerName": "Customer 1",
      "status": "NEW",
      "amount": 21.00,
      "createdAt": "2024-01-11T00:00:00Z",
      "updatedAt": "2024-01-11T00:00:00Z"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "customerName": "Customer 3",
      "status": "NEW",
      "amount": 23.00,
      "createdAt": "2024-01-13T00:00:00Z",
      "updatedAt": "2024-01-13T00:00:00Z"
    }
  ],
  "page": 1,
  "limit": 10,
  "totalItems": 9,
  "totalPages": 1
}
```

**Sorting:**
- Default: `createdAt DESC` (newest orders first)

## Error Handling

API returns structured errors:

### 400 Bad Request
Invalid request parameters (e.g., negative page number).

```json
{
  "timestamp": "2024-02-18T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Page must be >= 1",
  "path": "/orders"
}
```

### 422 Unprocessable Entity
Request body validation errors.

```json
{
  "timestamp": "2024-02-18T10:30:00Z",
  "status": 422,
  "error": "Validation Failed",
  "errors": [
    "customerName: must not be blank",
    "amount: must be greater than 0"
  ],
  "path": "/orders"
}
```

### 500 Internal Server Error
Internal server error.

```json
{
  "timestamp": "2024-02-18T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Unexpected error occurred",
  "path": "/orders"
}
```

## Technical Details

### Stack
- **Framework:** Spring Boot 3.4.2
- **Java:** 21
- **Database:** H2 2.4.240
- **ORM:** Hibernate 7.2.1 (JPA)
- **Build:** Gradle 8.12
- **Testing:** JUnit 5, MockMvc, Spring Boot Test

### Key Features
- ✅ RESTful API design
- ✅ JPA Specifications for dynamic filtering
- ✅ Validation using Bean Validation (JSR-380)
- ✅ Global exception handling (@RestControllerAdvice)
- ✅ Pagination with metadata
- ✅ Range filters (inclusive)
- ✅ 85% test coverage
- ✅ Deterministic data seeding

### Filtering Implementation
Uses `JPA Specification API` for building dynamic queries:

```java
Specification<Order> spec = Specification.where(null);
if (status != null) spec = spec.and(hasStatus(status));
if (minAmount != null) spec = spec.and(hasAmountGreaterThanOrEqualTo(minAmount));
// ...
Page<Order> result = orderRepository.findAll(spec, pageable);
```

### Date Handling
- All dates stored in UTC (`Instant` type)
- Serialization to ISO-8601 format
- Range filtering (inclusive): `fromDate <= createdAt <= toDate`

## Development

### Run tests with coverage
```powershell
./gradlew.bat clean test jacocoTestReport
```

### Build without tests
```powershell
./gradlew.bat build -x test
```

### Clean build
```powershell
./gradlew.bat clean build
```

## Test Suite

**Total tests:** 15 integration tests

| Test Category | Count | Coverage |
|---------------|-------|----------|
| Pagination | 3 | Basic, limits, edge cases |
| Filtering | 5 | Status, amount range, date range, combined |
| Validation | 4 | Invalid inputs, missing fields |
| CRUD | 2 | Create, list |
| Edge Cases | 1 | Out of range page |

**Test stability:**
- Uses `@Transactional` for isolation
- Deterministic data (seed 424242)
- Database cleanup before each test

## Known Limitations
- No authentication/authorization
- No UPDATE/DELETE operations support
- Maximum page size: 100 items
- In-memory database (data not persisted after restart)

## Future Enhancements
- [ ] PATCH/DELETE endpoints
- [ ] Sorting (by amount, date, name)
- [ ] Full-text search by customer name
- [ ] Change audit (created_by, updated_by)
- [ ] Migration to PostgreSQL for production
- [ ] Docker container
- [ ] OpenAPI/Swagger documentation

## License
MIT

## Author
Developed with GitHub Copilot assistance (70% code generation, 85% test coverage)
#   ai-assisted-development
 
 
