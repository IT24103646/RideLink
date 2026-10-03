# RideLink Fare and Payment Service

Fare and Payment Service owns fare estimates, simulated payment records, and JSON receipts. It runs independently on port `8083` and stores data in MongoDB database `fare_payment_db`.

## Architecture

This service communicates with Ride Service only through REST:

- Fare Service: `http://localhost:8083`
- Ride Service dependency: `http://localhost:8082`
- MongoDB database: `fare_payment_db`
- Collections: `fare_estimates`, `payments`

The Ride Service contract currently returns `id`, passenger/driver IDs, pickup, destination, status, and lifecycle timestamps. It does not return distance or duration. Therefore this service accepts estimated/actual distance and duration in its own requests and does not modify Ride Service to invent those fields.

## Configuration

| Variable | Default | Purpose |
|---|---:|---|
| `SERVER_PORT` | `8083` | HTTP port |
| `MONGODB_URI` | `mongodb://localhost:27017/fare_payment_db` | Service-owned MongoDB |
| `JWT_SECRET` | required | Account Service-compatible signing secret, at least 32 bytes |
| `JWT_EXPIRATION_MS` | `3600000` | JWT expiration reference |
| `RIDE_SERVICE_BASE_URL` | `http://localhost:8082` | Ride Service REST base URL |
| `BASE_FARE` | `100.00` | Project-defined base fare |
| `PER_KM_RATE` | `80.00` | Project-defined per-kilometre rate |
| `PER_MINUTE_RATE` | `10.00` | Project-defined per-minute rate |

Fare rates are configurable project values, not claims about assignment-provided rates. No secrets or tokens belong in source control.

## Fare formula

```text
Distance Charge = distanceKm * perKmRate
Duration Charge = durationMinutes * perMinuteRate
Total Fare = baseFare + distanceCharge + durationCharge
```

Money uses `BigDecimal`, scale 2, and `HALF_UP` rounding.

## API

All business endpoints require a Bearer JWT. Claims follow the existing Account Service contract: `sub` is Account ID and `role` is `PASSENGER`, `DRIVER`, or `ADMIN`.

| Method | Path | Access |
|---|---|---|
| POST | `/api/fares/estimate` | PASSENGER, ADMIN |
| GET | `/api/fares/estimates/{estimateId}` | owner or ADMIN |
| POST | `/api/payments` | PASSENGER, ADMIN |
| GET | `/api/payments/{paymentId}` | owner passenger, assigned driver, ADMIN |
| GET | `/api/payments/ride/{rideId}` | ride passenger, driver authorized by Ride Service, ADMIN |
| GET | `/api/payments/{paymentId}/receipt` | owner passenger, ride driver, ADMIN |

### Estimate example

```json
{
  "pickup": "SLIIT Malabe Campus",
  "destination": "Colombo Fort",
  "estimatedDistanceKm": 18.5,
  "estimatedDurationMinutes": 42
}
```

### Payment example

```json
{
  "rideId": "RIDE_ID",
  "actualDistanceKm": 17.8,
  "actualDurationMinutes": 39,
  "paymentMethod": "CARD",
  "simulateFailure": false
}
```

Payments are simulated only. `false` records `SUCCESS`, a transaction reference, receipt number, and `paidAt`. `true` records `FAILED` with a safe failure reason and no receipt, returning HTTP `402`. Successful duplicate payments for a ride are rejected with HTTP `409`; the database also has a sparse unique index for the successful ride key.

Payment is allowed only when the Ride Service response status is `COMPLETED`. Missing rides return `404`; Ride Service connection failure returns `503`.

## Error codes

`VALIDATION_ERROR`, `ESTIMATE_NOT_FOUND`, `PAYMENT_NOT_FOUND`, `RIDE_NOT_FOUND`, `FORBIDDEN_PAYMENT_ACCESS`, `PAYMENT_ALREADY_EXISTS`, `PAYMENT_NOT_ALLOWED_FOR_RIDE_STATE`, `RIDE_SERVICE_UNAVAILABLE`, `RIDE_SERVICE_ERROR`, `RECEIPT_NOT_AVAILABLE`, and `INTERNAL_ERROR`.

## Run and verify

```powershell
$env:JWT_SECRET="replace-with-a-development-secret-at-least-32-bytes"
.\mvnw.cmd clean test
.\mvnw.cmd -DskipTests package
java -jar target\fare-payment-service-0.0.1-SNAPSHOT.jar
```

Swagger UI: `http://localhost:8083/swagger-ui.html`
OpenAPI JSON: `http://localhost:8083/v3/api-docs`

Import `Fare-Payment-Service.postman_collection.json` and `Fare-Payment-Service.postman_environment.json` for request examples. The environment contains placeholders only and requires a real locally-issued JWT and a completed Ride Service ride for end-to-end payment requests.

## Testing scope

Unit tests cover configurable fare calculation, rounding, successful payment, simulated failure, incomplete ride rejection, and duplicate payment rejection. They mock Ride Service and do not require an external payment provider. Runtime payment verification additionally requires MongoDB, Ride Service, and a valid Account Service JWT.
