# Orders API (Spring Boot)

## Overview
REST API for order management with **server-side pagination and filtering**.  
Main endpoint for this assignment: `GET /api/orders`.

The API supports:
- pagination (`page`, `limit`)
- filtering by `status`
- filtering by `amount` range (`minAmount`, `maxAmount`)
- filtering by date range (`fromDate`, `toDate`)

## Requirements
- Java 21
- Gradle 8.x (wrapper included)

## Quick Start

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew bootRun
```

Application starts on `http://localhost:8080`.

### Test
```bash
./gradlew test
```

---

## API

### POST /api/orders
Create a new order.

**Request body**
```json
{
  "customerName": "Alice Johnson",
  "status": "NEW",
  "amount": 125.50
}
```

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

### GET /api/orders
Returns paginated list of orders with filters.

#### Query Parameters

| Parameter | Type | Default | Validation | Description |
|---|---|---|---|---|
| `page` | int | `1` | `>= 1` | 1-based page number |
| `limit` | int | `10` | `1..100` | page size |
| `status` | enum | - | `NEW, PAID, SHIPPED, CANCELLED` | filter by status |
| `minAmount` | decimal | - | optional | minimum amount (inclusive) |
| `maxAmount` | decimal | - | `>= minAmount` (if both passed) | maximum amount (inclusive) |
| `fromDate` | ISO-8601 instant | - | optional | start of period (inclusive) |
| `toDate` | ISO-8601 instant | - | `>= fromDate` (if both passed) | end of period (inclusive) |

#### Pagination behavior
- Offset is calculated server-side from page and limit via Spring Data `PageRequest.of(page - 1, limit, sort)`.
- Default sorting: `createdAt DESC`.
- Metadata is returned in response: `page`, `limit`, `totalItems`, `totalPages`.

#### Request examples

Basic (defaults):
```bash
curl "http://localhost:8080/api/orders"
```

Custom pagination:
```bash
curl "http://localhost:8080/api/orders?page=2&limit=15"
```

Filter by status:
```bash
curl "http://localhost:8080/api/orders?status=PAID"
```

Filter by amount range:
```bash
curl "http://localhost:8080/api/orders?minAmount=50.00&maxAmount=150.00"
```

Filter by date range:
```bash
curl "http://localhost:8080/api/orders?fromDate=2024-01-10T00:00:00Z&toDate=2024-01-15T23:59:59Z"
```

Combined filters + pagination:
```bash
curl "http://localhost:8080/api/orders?status=NEW&minAmount=20.00&fromDate=2024-01-05T00:00:00Z&toDate=2024-01-20T00:00:00Z&page=1&limit=10"
```

#### Response example
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
    }
  ],
  "page": 1,
  "limit": 10,
  "totalItems": 9,
  "totalPages": 1
}
```

---

## Validation and Error Handling

`400 Bad Request` is returned for invalid query params, e.g.:
- `page=0`
- `limit=101`
- `maxAmount < minAmount`
- `toDate < fromDate`
- invalid enum/date format

---

## Testing scope
Integration tests cover:
- default pagination
- custom page/limit
- invalid pagination values
- status filtering
- amount range filtering
- date range filtering
- combined filters
- empty result set
- invalid ranges and date format

Run:
```bash
./gradlew test
```

---

## Notes for assignment reviewers
This repository is adapted for the task **“Add pagination + server-side filtering to GET /api/orders using AI assistant.”**  
See `copilot-metrics.md` for AI contribution breakdown, accepted suggestions, manual fixes, and time-saved estimate.
