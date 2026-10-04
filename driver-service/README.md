# Driver Service

The Driver Service runs on port `8081` and stores driver profiles in MongoDB database `driver_db`.

## Authentication

User endpoints use the environment-configured JWT secret:

- `JWT_SECRET`
- `JWT_EXPIRATION_MS` (optional)

The public endpoint `PATCH /api/drivers/{id}/availability` remains restricted to `ROLE_ADMIN`.

Ride Service integration uses the separate internal endpoint:

- `PATCH /api/drivers/{id}/availability/internal`
- Header: `X-Service-Key: <DRIVER_SERVICE_INTERNAL_KEY>`

Set the same `DRIVER_SERVICE_INTERNAL_KEY` environment value in Driver Service and Ride Service. Do not commit it, put it in Postman, or use a user password or JWT as this credential. The service key is accepted only by the internal availability endpoint.