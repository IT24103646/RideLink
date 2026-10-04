# Ride Service

## Responsibility
The Ride Service handles ride request creation, assignment, lifecycle state changes, and ride retrieval for passengers and drivers. It stores its own ride data in the MongoDB `rides` collection and communicates with the Driver Service for driver availability and assignment decisions.

## Port and database
- Port: `8082`
- MongoDB: `mongodb://localhost:27017/ride_db`
- Collection: `rides`

## Prerequisites
- Java 21
- Maven
- MongoDB running on localhost
- Driver Service running on `http://localhost:8081`
- JWT secret configured in `JWT_SECRET`

## Environment variables
- `JWT_SECRET`
- `JWT_EXPIRATION_MS` (default: `3600000`)
- `SERVER_PORT` (default: `8082`)
- `MONGODB_URI` (default: `mongodb://localhost:27017/ride_db`)
- `DRIVER_SERVICE_BASE_URL` (default: `http://localhost:8081`)
- `DRIVER_SERVICE_INTERNAL_KEY` (required for ride-to-driver availability updates; configure the same local secret in both services)

## Start
```bash
./mvnw spring-boot:run
```

## Driver Service startup
```bash
cd ../driver-service/driver-service
./mvnw spring-boot:run
```

## Authentication
All business endpoints require a valid JWT bearer token. Passenger and driver roles are enforced with method-level security.

## Driver assignment rule
The Ride Service calls `GET /api/drivers/available` and selects the first eligible driver returned by Driver Service. It then updates that driver's availability to `OFFLINE` using the Driver Service contract before persisting the assigned ride.

Ride Service sends the passenger bearer token for driver discovery and uses `X-Service-Key` from `DRIVER_SERVICE_INTERNAL_KEY` only for the internal availability update endpoint. Never commit this value or place it in Postman.

## Ride lifecycle
REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
Cancellation is allowed from REQUESTED, ASSIGNED, and ACCEPTED only.

## Cancellation rules
- Passenger may cancel their own ride while it is REQUESTED, ASSIGNED, or ACCEPTED.
- Driver may cancel only their assigned ride while it is ASSIGNED or ACCEPTED.
- Admin may cancel a ride while it is REQUESTED, ASSIGNED, or ACCEPTED.
- Cancellation is rejected once the ride is IN_PROGRESS or COMPLETED.

## Downstream failure behavior
If Driver Service is unavailable, the ride creation flow returns `503` with the stable error code `DRIVER_SERVICE_UNAVAILABLE` and the message `A driver cannot be assigned now. Please retry.`

## Swagger
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- Swagger UI: `http://localhost:8082/swagger-ui.html`

## Postman usage
Import the included Postman collection and set `baseUrl`, `rideServiceUrl`, `driverServiceUrl`, and `jwt` as environment variables.

## Tests
```bash
./mvnw test
```
