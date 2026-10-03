# Account Service

The Account Service runs on port `8080` and stores accounts in MongoDB database `account_db`.

## API

- `POST /api/accounts/register` creates a PASSENGER or DRIVER account. ADMIN registration is not public.
- `POST /api/accounts/login` issues a JWT containing the account id as `sub`, plus `email` and `role` claims.
- `GET /api/accounts/me` returns the authenticated account profile.
- `PUT /api/accounts/me` updates the authenticated account name and phone.
- `PATCH /api/accounts/{id}/role` changes a role and is restricted to ADMIN.
- `PATCH /api/accounts/{id}/status` changes account status and is restricted to ADMIN.

Use `JWT_SECRET` for the signing key and `MONGODB_URI` to override the default MongoDB connection.

Swagger UI is available at `/swagger-ui.html` and the OpenAPI document at `/v3/api-docs`.