# SmartLedger API Test Flows Documentation

This document provides comprehensive test flows for testing SmartLedger API endpoints using Postman or Swagger UI. Each endpoint includes detailed explanations, request/response formats, and testing scenarios.

---

## Table of Contents

1. [Authentication Flow](#authentication-flow)
2. [Service Credential Management](#service-credential-management)
3. [User Management](#user-management)
4. [Fee Management](#fee-management)
5. [Payment Management](#payment-management)
   - [Pay Fee Invoice with Payment Gateway (Automatic)](#22-pay-fee-invoice-with-payment-gateway-automatic)
   - [Initiate Generic Gateway Payment](#23-initiate-generic-gateway-payment)
   - [Verify Payment with Gateway](#24-verify-payment-with-gateway)
   - [Paystack Payment Callback (Enhanced)](#25-paystack-payment-callback-enhanced)
   - [Payment Gateway Integration Flow](#payment-gateway-integration-flow)
   - [Payment Gateway Configuration](#payment-gateway-configuration)
   - [Payment Gateway Testing Scenarios](#payment-gateway-testing-scenarios)
   - [Payment Gateway Error Handling](#payment-gateway-error-handling)
6. [Ledger/Account Management](#ledgeraccount-management)
7. [Journal Entries](#journal-entries)
8. [Reporting](#reporting)
9. [AI Insights](#ai-insights)

---

## Authentication Flow

### Overview
The authentication system uses JWT (JSON Web Tokens) for stateless authentication. Users must authenticate first to receive a token, then include this token in subsequent requests.

### Flow Architecture
```
1. Login → Receive JWT Token
2. Include Token in Authorization Header (Bearer token)
3. Access Protected Endpoints
4. Token Expiration → Re-authenticate
```

---

### 1. User Login

**Endpoint:** `POST /api/v1/auth/login`

**Purpose:** Authenticate user credentials and receive JWT token for subsequent API calls.

**Request:**
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Request Details:**
- `username`: The user's unique username (required)
- `password`: The user's password (required)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin",
    "email": "admin.smartledger@gmail.com"
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "type": "https://api.smartledger.com/errors/ERR-1001",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Bad credentials",
  "instance": "/api/v1/auth/login"
}
```

**Testing Scenarios:**
1. **Valid Login:** Use correct username/password → Should return 200 with JWT token
2. **Invalid Password:** Use wrong password → Should return 401 Unauthorized
3. **Non-existent User:** Use username that doesn't exist → Should return 401 Unauthorized
4. **Missing Fields:** Send incomplete request → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/auth/login`
- Headers: `Content-Type: application/json`
- Body: Raw JSON with username/password

**Important Notes:**
- Copy the `token` from response for subsequent requests
- Default admin credentials: username=`admin`, password=`admin`
- Token expiration is configured in application properties

---

### 2. Forgot Password (Initiate Reset)

**Endpoint:** `POST /api/v1/auth/forgot-password`

**Purpose:** Initiate password reset process by sending reset link to user's email.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Request Details:**
- `email`: User's registered email address (required)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "If the email exists in our system, a password reset link has been sent",
  "data": null
}
```

**Security Note:** This endpoint always returns success, even if email doesn't exist. This prevents account enumeration attacks where attackers could determine which emails are registered.

**Testing Scenarios:**
1. **Existing Email:** Send registered email → Should return 200 (check logs for email sent)
2. **Non-existent Email:** Send unregistered email → Should return 200 (security measure)
3. **Invalid Email Format:** Send malformed email → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/auth/forgot-password`
- Headers: `Content-Type: application/json`
- Body: Raw JSON with email

**Important Notes:**
- Requires email service configuration (MAIL_USERNAME, MAIL_PASSWORD)
- In development, check logs for reset token if email service is not configured
- Reset token expires in 1 hour by default

---

### 3. Reset Password (Complete Reset)

**Endpoint:** `POST /api/v1/auth/reset-password`

**Purpose:** Complete password reset using the token received via email.

**Request:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePassword123"
}
```

**Request Details:**
- `token`: The reset token received in email (required)
- `newPassword`: New password for the user (required)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Password has been reset successfully",
  "data": null
}
```

**Error Response (400 Bad Request):**
```json
{
  "type": "https://api.smartledger.com/errors/ERR-3006",
  "title": "Invalid Password Reset Token",
  "status": 400,
  "detail": "The provided reset token is invalid or has expired",
  "instance": "/api/v1/auth/reset-password"
}
```

**Testing Scenarios:**
1. **Valid Token:** Use token from forgot-password → Should return 200
2. **Invalid Token:** Use random/incorrect token → Should return 400
3. **Expired Token:** Use token older than 1 hour → Should return 400
4. **Used Token:** Reuse already-used token → Should return 400

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/auth/reset-password`
- Headers: `Content-Type: application/json`
- Body: Raw JSON with token and newPassword

**Important Notes:**
- Token can only be used once
- After successful reset, user can login with new password
- Token is marked as used after successful reset

---

## Authentication Flow Summary

### Complete Password Reset Flow
```
1. User forgets password
   ↓
2. POST /api/v1/auth/forgot-password with email
   ↓
3. System sends email with reset token
   ↓
4. User receives email, extracts token
   ↓
5. POST /api/v1/auth/reset-password with token + new password
   ↓
6. Password updated, user can login with new credentials
```

### Using JWT Token in Subsequent Requests

After successful login, include the JWT token in the Authorization header:

**Header Format:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Postman Setup:**
1. Go to Authorization tab
2. Type: Bearer Token
3. Token: Paste the JWT token from login response

**Swagger UI Setup:**
1. Click "Authorize" button
2. Enter: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. Click Authorize

---

## Service Credential Management

### Overview
Service credentials provide API key authentication for external systems (like fee portals) to interact with SmartLedger without requiring user accounts. Each credential has a cryptographically generated API key and specific granted permissions.

### Authentication Methods
SmartLedger supports two authentication methods:
1. **JWT Authentication** - For human users (see Authentication Flow section)
2. **API Key Authentication** - For service-to-service communication (this section)

### API Key Authentication Flow
```
1. Admin creates service credential → Receives API key (shown only once)
2. External service stores API key securely
3. External service includes API key in X-Service-Api-Key header
4. ServiceApiKeyAuthenticationFilter validates and grants permissions
5. External service accesses permitted endpoints
```

### Security Notes
- API keys are generated using cryptographically secure random bytes (256 bits)
- API keys are hashed with BCrypt before storage (never stored in plain text)
- Raw API key is shown ONLY on creation - never retrievable again
- Disabled credentials cannot authenticate even with valid API key
- Each credential has specific granted permissions (not full admin access)

---

### 1. Create Service Credential

**Endpoint:** `POST /api/v1/service-credentials`

**Purpose:** Create a new service credential with generated API key for external system authentication.

**Authentication:** Requires `SERVICE_CREDENTIAL:MANAGE` permission (JWT required)

**Request:**
```json
{
  "name": "fee-portal-production",
  "grantedPermissions": ["PAYMENT:CREATE", "PAYMENT:READ"]
}
```

**Request Details:**
- `name`: Unique identifier for the service (required)
- `grantedPermissions`: Set of permission codes granted to this credential (required)

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Service credential created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "name": "fee-portal-production",
    "grantedPermissions": ["PAYMENT:CREATE", "PAYMENT:READ"],
    "enabled": true,
    "createdAt": "2024-08-15T10:00:00Z",
    "updatedAt": "2024-08-15T10:00:00Z",
    "apiKey": "aB3xY9zK2mN4pQ7rT1vW5xY8zA2bC4dE6fG8hJ0kL2mN4pQ7rT1vW5xY8zA2bC4dE"
  }
}
```

**Important:** The `apiKey` field is shown ONLY in the creation response. Save it securely - it cannot be retrieved again.

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "message": "Service credential with this name already exists",
  "data": null
}
```

**Testing Scenarios:**
1. **Valid Creation:** All fields correct → Should return 201 with API key
2. **Duplicate Name:** Name already exists → Should return 409 Conflict
3. **Empty Permissions:** No permissions granted → Should return 400 Bad Request
4. **Missing Name:** Name field missing → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/service-credentials`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>` (admin token required)
- Body: Raw JSON with name and grantedPermissions

**Important Notes:**
- Copy the `apiKey` immediately - it won't be shown again
- Store the API key securely (environment variables, secret management)
- Grant only the minimum permissions needed
- Use descriptive names (e.g., "fee-portal-production", "reporting-service")

---

### 2. List Service Credentials

**Endpoint:** `GET /api/v1/service-credentials`

**Purpose:** List all service credentials (without API keys).

**Authentication:** Requires `SERVICE_CREDENTIAL:MANAGE` permission (JWT required)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Service credentials retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440020",
      "name": "fee-portal-production",
      "grantedPermissions": ["PAYMENT:CREATE", "PAYMENT:READ"],
      "enabled": true,
      "createdAt": "2024-08-15T10:00:00Z",
      "updatedAt": "2024-08-15T10:00:00Z",
      "apiKey": null
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440021",
      "name": "reporting-service",
      "grantedPermissions": ["REPORT:GENERATE"],
      "enabled": true,
      "createdAt": "2024-08-15T11:00:00Z",
      "updatedAt": "2024-08-15T11:00:00Z",
      "apiKey": null
    }
  ]
}
```

**Important:** The `apiKey` field is always `null` in list responses for security.

**Testing Scenarios:**
1. **List All:** No filters → Should return all credentials
2. **Empty List:** No credentials exist → Should return empty array

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/service-credentials`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>` (admin token required)

---

### 3. Disable Service Credential

**Endpoint:** `PATCH /api/v1/service-credentials/{id}/disable`

**Purpose:** Disable a service credential (revokes access without deletion).

**Authentication:** Requires `SERVICE_CREDENTIAL:MANAGE` permission (JWT required)

**URL Parameters:**
- `id`: UUID of the service credential (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Service credential disabled successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "name": "fee-portal-production",
    "grantedPermissions": ["PAYMENT:CREATE", "PAYMENT:READ"],
    "enabled": false,
    "createdAt": "2024-08-15T10:00:00Z",
    "updatedAt": "2024-08-15T12:00:00Z",
    "apiKey": null
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "message": "Service credential not found",
  "data": null
}
```

**Testing Scenarios:**
1. **Valid Disable:** Credential exists → Should return 200 with enabled=false
2. **Non-existent ID:** Invalid UUID → Should return 404 Not Found
3. **Already Disabled:** Already disabled → Should return 200 (idempotent)

**Postman Setup:**
- Method: PATCH
- URL: `http://localhost:8081/api/v1/service-credentials/{id}/disable`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>` (admin token required)

**Important Notes:**
- Disabling is reversible (use enable endpoint)
- Disabled credentials cannot authenticate even with valid API key
- Useful for temporary revocation without losing the credential

---

### 4. Enable Service Credential

**Endpoint:** `PATCH /api/v1/service-credentials/{id}/enable`

**Purpose:** Re-enable a previously disabled service credential.

**Authentication:** Requires `SERVICE_CREDENTIAL:MANAGE` permission (JWT required)

**URL Parameters:**
- `id`: UUID of the service credential (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Service credential enabled successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "name": "fee-portal-production",
    "grantedPermissions": ["PAYMENT:CREATE", "PAYMENT:READ"],
    "enabled": true,
    "createdAt": "2024-08-15T10:00:00Z",
    "updatedAt": "2024-08-15T13:00:00Z",
    "apiKey": null
  }
}
```

**Testing Scenarios:**
1. **Valid Enable:** Disabled credential → Should return 200 with enabled=true
2. **Non-existent ID:** Invalid UUID → Should return 404 Not Found
3. **Already Enabled:** Already enabled → Should return 200 (idempotent)

**Postman Setup:**
- Method: PATCH
- URL: `http://localhost:8081/api/v1/service-credentials/{id}/enable`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>` (admin token required)

---

### Using API Key Authentication

Once you have a service credential with API key, external systems can authenticate using the API key instead of JWT.

**Header Format:**
```
X-Service-Api-Key: aB3xY9zK2mN4pQ7rT1vW5xY8zA2bC4dE6fG8hJ0kL2mN4pQ7rT1vW5xY8zA2bC4dE
```

**Postman Setup:**
1. Go to Headers tab
2. Add new header: `X-Service-Api-Key`
3. Value: Paste your API key

**Example Request with API Key:**
```http
POST /api/payment/payments
X-Service-Api-Key: aB3xY9zK2mN4pQ7rT1vW5xY8zA2bC4dE6fG8hJ0kL2mN4pQ7rT1vW5xY8zA2bC4dE
Content-Type: application/json

{
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "PAYSTACK"
}
```

**Error Response (401 Unauthorized) - Invalid API Key:**
```json
{
  "error": "Invalid API key"
}
```

**Error Response (401 Unauthorized) - Disabled Credential:**
```json
{
  "error": "Service credential is disabled"
}
```

**Error Response (403 Forbidden) - Insufficient Permissions:**
```json
{
  "type": "https://api.smartledger.com/errors/ERR-1003",
  "title": "Forbidden",
  "status": 403,
  "detail": "Access denied",
  "instance": "/api/payment/payments"
}
```

**Testing Scenarios:**
1. **Valid API Key:** Correct API key with sufficient permissions → Should return 200
2. **Invalid API Key:** Wrong API key → Should return 401 Unauthorized
3. **Disabled Credential:** Valid API key but credential disabled → Should return 401 Unauthorized
4. **Insufficient Permissions:** Valid API key but missing required permission → Should return 403 Forbidden
5. **No Authentication:** No JWT or API key → Should return 401 Unauthorized

**Important Notes:**
- API key authentication is checked BEFORE JWT authentication
- If no API key header is present, the system falls back to JWT authentication
- Permissions are enforced based on the credential's grantedPermissions set
- Each credential should have the minimum permissions needed for its purpose

---

## User Management

### 5. Create User

**Endpoint:** `POST /api/v1/users`

**Purpose:** Create a new user in the system.

**Authentication:** Requires `USER:CREATE` permission

**Request:**
```json
{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true
  }
}
```

**Testing Scenarios:**
1. **Valid User Creation:** All fields correct → Should return 201
2. **Duplicate Username:** Username already exists → Should return 409 Conflict
3. **Duplicate Email:** Email already exists → Should return 409 Conflict
4. **Invalid Email Format:** Malformed email → Should return 400 Bad Request
5. **Weak Password:** Password doesn't meet requirements → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/users`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with user details

---

### 6. Get User by ID

**Endpoint:** `GET /api/v1/users/{userId}`

**Purpose:** Retrieve user details by user ID.

**Authentication:** Requires `USER:READ` permission

**URL Parameters:**
- `userId`: UUID of the user (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "enabled": true,
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "type": "https://api.smartledger.com/errors/ERR-3005",
  "title": "Not Found",
  "status": 404,
  "detail": "User not found",
  "instance": "/api/v1/users/550e8400-e29b-41d4-a716-446655440001"
}
```

**Testing Scenarios:**
1. **Valid User ID:** Existing user → Should return 200 with user details
2. **Invalid User ID:** Non-existent UUID → Should return 404 Not Found
3. **Invalid UUID Format:** Malformed UUID → Should return 400 Bad Request

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/users/{userId}`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

### 7. Update User

**Endpoint:** `PUT /api/v1/users/{userId}`

**Purpose:** Update user information.

**Authentication:** Requires `USER:UPDATE` permission

**URL Parameters:**
- `userId`: UUID of the user (path parameter)

**Request:**
```json
{
  "firstName": "John Updated",
  "lastName": "Doe Updated",
  "phone": "+1234567890"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John Updated",
    "lastName": "Doe Updated",
    "phone": "+1234567890"
  }
}
```

**Testing Scenarios:**
1. **Valid Update:** Correct user ID and data → Should return 200
2. **Non-existent User:** Invalid user ID → Should return 404 Not Found
3. **Invalid Phone Format:** Malformed phone number → Should return 400 Bad Request

**Postman Setup:**
- Method: PUT
- URL: `http://localhost:8081/api/v1/users/{userId}`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with updated fields

---

### 8. Change Password

**Endpoint:** `PUT /api/v1/users/{userId}/password`

**Purpose:** User changes their own password.

**Authentication:** Requires user to be authenticated (can only change own password)

**URL Parameters:**
- `userId`: UUID of the user (path parameter)

**Request:**
```json
{
  "oldPassword": "OldPassword123",
  "newPassword": "NewSecurePassword456"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

**Error Response (400 Bad Request):**
```json
{
  "type": "https://api.smartledger.com/errors/ERR-3001",
  "title": "Bad Request",
  "status": 400,
  "detail": "Current password is incorrect",
  "instance": "/api/v1/users/550e8400-e29b-41d4-a716-446655440001/password"
}
```

**Testing Scenarios:**
1. **Valid Password Change:** Correct old password → Should return 200
2. **Incorrect Old Password:** Wrong old password → Should return 400
3. **Weak New Password:** New password too weak → Should return 400
4. **Same Password:** New password same as old → Should return 400

**Postman Setup:**
- Method: PUT
- URL: `http://localhost:8081/api/v1/users/{userId}/password`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with oldPassword and newPassword

---

### 9. Grant Role to User

**Endpoint:** `POST /api/v1/users/{userId}/roles`

**Purpose:** Assign a role to a user.

**Authentication:** Requires `USER:ASSIGN_ROLE` permission

**URL Parameters:**
- `userId`: UUID of the user (path parameter)

**Request:**
```json
{
  "roleId": "550e8400-e29b-41d4-a716-446655440002"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Role granted successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440001",
    "roleId": "550e8400-e29b-41d4-a716-446655440002",
    "roleName": "ACCOUNTANT"
  }
}
```

**Testing Scenarios:**
1. **Valid Role Assignment:** Existing user and role → Should return 200
2. **Duplicate Role:** Role already assigned → Should return 409 Conflict
3. **Non-existent User:** Invalid user ID → Should return 404 Not Found
4. **Non-existent Role:** Invalid role ID → Should return 404 Not Found

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/users/{userId}/roles`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with roleId

---

### 10. Revoke Role from User

**Endpoint:** `DELETE /api/v1/users/{userId}/roles/{roleId}`

**Purpose:** Remove a role from a user.

**Authentication:** Requires `USER:ASSIGN_ROLE` permission

**URL Parameters:**
- `userId`: UUID of the user (path parameter)
- `roleId`: UUID of the role (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Role revoked successfully",
  "data": null
}
```

**Testing Scenarios:**
1. **Valid Role Revocation:** User has the role → Should return 200
2. **Role Not Assigned:** User doesn't have the role → Should return 404 Not Found
3. **Non-existent User:** Invalid user ID → Should return 404 Not Found

**Postman Setup:**
- Method: DELETE
- URL: `http://localhost:8081/api/v1/users/{userId}/roles/{roleId}`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

## Fee Management

### 11. Create Fee Schedule

**Endpoint:** `POST /api/fees/schedules`

**Purpose:** Create a new fee schedule for a specific academic term/period.

**Authentication:** Requires `FEE:CREATE` permission

**Request:**
```json
{
  "code": "FEE-2024-10",
  "name": "Grade 10 Fees",
  "academicYear": "2024-2025",
  "term": "Term 1",
  "grade": "Grade 10",
  "effectiveDate": "2024-09-01",
  "expiryDate": "2025-08-31",
  "description": "Standard fee schedule for Grade 10"
}
```

**Request Details:**
- `code`: Unique schedule code (required)
- `name`: Human-readable name (required)
- `academicYear`: Academic year (required)
- `term`: Term/semester (required)
- `grade`: Grade level (required)
- `effectiveDate`: When schedule becomes effective (required)
- `expiryDate`: When schedule expires (required)
- `description`: Additional details (optional)

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Fee schedule created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "code": "FEE-2024-10",
    "name": "Grade 10 Fees",
    "academicYear": "2024-2025",
    "term": "Term 1",
    "grade": "Grade 10",
    "status": "DRAFT"
  }
}
```

**Testing Scenarios:**
1. **Valid Schedule Creation:** All fields correct → Should return 201
2. **Duplicate Code:** Code already exists → Should return 409 Conflict
3. **Invalid Date Range:** Expiry before effective → Should return 400 Bad Request
4. **Missing Required Fields:** Incomplete request → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/schedules`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with schedule details

---

### 12. Add Fee Item to Schedule

**Endpoint:** `POST /api/fees/schedules/{scheduleId}/items`

**Purpose:** Add individual fee items (tuition, books, etc.) to a fee schedule.

**Authentication:** Requires `FEE:UPDATE` permission

**URL Parameters:**
- `scheduleId`: UUID of the fee schedule (path parameter)

**Request:**
```json
{
  "feeType": "TUITION_FEE",
  "amount": 5000.00,
  "currency": "USD",
  "mandatory": true,
  "name": "Tuition Fee",
  "description": "Annual tuition fee"
}
```

**Request Details:**
- `feeType`: Type of fee (TUITION_FEE, BOOK_FEE, LAB_FEE, etc.) (required)
- `amount`: Fee amount (required)
- `currency`: Currency code (required)
- `mandatory`: Whether fee is compulsory (required)
- `name`: Fee item name (required)
- `description`: Additional details (optional)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Fee item added successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440004",
    "feeType": "TUITION_FEE",
    "amount": 5000.00,
    "currency": "USD",
    "mandatory": true
  }
}
```

**Testing Scenarios:**
1. **Valid Fee Item:** All fields correct → Should return 200
2. **Invalid Fee Type:** Not a valid fee type → Should return 400 Bad Request
3. **Negative Amount:** Amount less than 0 → Should return 400 Bad Request
4. **Invalid Currency:** Not a valid ISO currency code → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/schedules/{scheduleId}/items`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with fee item details

---

### 13. Activate Fee Schedule

**Endpoint:** `POST /api/fees/schedules/{scheduleId}/activate`

**Purpose:** Activate a fee schedule so it can be used for invoice generation.

**Authentication:** Requires `FEE:UPDATE` permission

**URL Parameters:**
- `scheduleId`: UUID of the fee schedule (path parameter)

**Request:**
```json
{
  "activatedBy": "admin"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Fee schedule activated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "code": "FEE-2024-10",
    "status": "ACTIVE",
    "activatedAt": "2024-08-15T10:00:00Z"
  }
}
```

**Testing Scenarios:**
1. **Valid Activation:** Draft schedule → Should return 200 with ACTIVE status
2. **Already Active:** Schedule already active → Should return 409 Conflict
3. **Non-existent Schedule:** Invalid schedule ID → Should return 404 Not Found

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/schedules/{scheduleId}/activate`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with activatedBy

---

### 14. Create Fee Invoice

**Endpoint:** `POST /api/fees/invoices`

**Purpose:** Create a new fee invoice for a student.

**Authentication:** Requires `FEE:CREATE` permission

**Request:**
```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440005",
  "academicYear": "2024-2025",
  "term": "Term 1",
  "grade": "Grade 10",
  "dueDate": "2024-09-30"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Fee invoice created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440006",
    "invoiceNumber": "INV-24-00001",
    "studentId": "550e8400-e29b-41d4-a716-446655440005",
    "status": "DRAFT",
    "totalAmount": 0.00,
    "dueDate": "2024-09-30"
  }
}
```

**Testing Scenarios:**
1. **Valid Invoice Creation:** All fields correct → Should return 201
2. **Non-existent Student:** Invalid student ID → Should return 404 Not Found
3. **Past Due Date:** Due date in the past → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/invoices`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with invoice details

---

### 15. Generate Invoice from Schedule

**Endpoint:** `POST /api/fees/invoices/generate-from-schedule`

**Purpose:** Automatically generate invoice with all fee items from a schedule.

**Authentication:** Requires `FEE:CREATE` permission

**Request:**
```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440005",
  "scheduleCode": "FEE-2024-10",
  "dueDate": "2024-09-30"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Invoice generated from schedule successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440006",
    "invoiceNumber": "INV-24-00001",
    "studentId": "550e8400-e29b-41d4-a716-446655440005",
    "status": "DRAFT",
    "totalAmount": 7500.00,
    "items": [
      {
        "feeType": "TUITION_FEE",
        "amount": 5000.00
      },
      {
        "feeType": "BOOK_FEE",
        "amount": 1500.00
      },
      {
        "feeType": "LAB_FEE",
        "amount": 1000.00
      }
    ]
  }
}
```

**Testing Scenarios:**
1. **Valid Generation:** Active schedule exists → Should return 201 with all items
2. **Inactive Schedule:** Schedule not active → Should return 400 Bad Request
3. **Non-existent Schedule:** Invalid schedule code → Should return 404 Not Found

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/invoices/generate-from-schedule`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with studentId, scheduleCode, and dueDate

---

### 16. Record Fee Payment

**Endpoint:** `POST /api/fees/invoices/{invoiceId}/payments`

**Purpose:** Record a payment against a fee invoice.

**Authentication:** Requires `FEE:UPDATE` permission

**URL Parameters:**
- `invoiceId`: UUID of the invoice (path parameter)

**Request:**
```json
{
  "feeType": "TUITION_FEE",
  "amount": 2500.00,
  "currency": "USD",
  "paymentMethod": "CASH",
  "reference": "REC-001"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Payment recorded successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440007",
    "invoiceId": "550e8400-e29b-41d4-a716-446655440006",
    "feeType": "TUITION_FEE",
    "amount": 2500.00,
    "currency": "USD",
    "status": "PENDING",
    "reference": "REC-001"
  }
}
```

**Testing Scenarios:**
1. **Valid Payment:** Invoice exists and amount valid → Should return 201
2. **Overpayment:** Amount exceeds remaining balance → Should return 400 Bad Request
3. **Non-existent Invoice:** Invalid invoice ID → Should return 404 Not Found
4. **Invalid Payment Method:** Not a valid payment method → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/invoices/{invoiceId}/payments`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with payment details

---

### 17. Complete Payment

**Endpoint:** `POST /api/fees/payments/{paymentId}/complete`

**Purpose:** Mark a pending payment as completed.

**Authentication:** Requires `FEE:UPDATE` permission

**URL Parameters:**
- `paymentId`: UUID of the payment (path parameter)

**Request:**
```json
{
  "receiptNumber": "REC-001-CONFIRMED"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment completed successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440007",
    "status": "COMPLETED",
    "receiptNumber": "REC-001-CONFIRMED",
    "completedAt": "2024-08-15T14:30:00Z"
  }
}
```

**Testing Scenarios:**
1. **Valid Completion:** Pending payment → Should return 200 with COMPLETED status
2. **Already Completed:** Payment already completed → Should return 409 Conflict
3. **Non-existent Payment:** Invalid payment ID → Should return 404 Not Found

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/payments/{paymentId}/complete`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with receiptNumber

---

## Payment Management

### 18. Get All Payments

**Endpoint:** `GET /api/v1/payments`

**Purpose:** Retrieve all payments with optional filtering.

**Authentication:** Requires `PAYMENT:READ` permission

**Query Parameters:**
- `status`: Filter by payment status (optional)
- `startDate`: Filter by start date (optional)
- `endDate`: Filter by end date (optional)
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

**Example Request:**
```
GET /api/v1/payments?status=COMPLETED&startDate=2024-01-01&endDate=2024-12-31&page=0&size=20
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payments retrieved successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440007",
        "amount": 2500.00,
        "currency": "USD",
        "status": "COMPLETED",
        "paymentMethod": "CASH",
        "createdAt": "2024-08-15T14:30:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

**Testing Scenarios:**
1. **All Payments:** No filters → Should return paginated list of all payments
2. **Filter by Status:** With status parameter → Should return filtered results
3. **Date Range:** With date parameters → Should return payments in range
4. **Empty Result:** No payments match filters → Should return empty array

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/payments` (add query parameters as needed)
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

### 19. Get Payment by ID

**Endpoint:** `GET /api/v1/payments/{paymentId}`

**Purpose:** Retrieve specific payment details.

**Authentication:** Requires `PAYMENT:READ` permission

**URL Parameters:**
- `paymentId`: UUID of the payment (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440007",
    "amount": 2500.00,
    "currency": "USD",
    "status": "COMPLETED",
    "paymentMethod": "CASH",
    "reference": "REC-001",
    "createdAt": "2024-08-15T14:30:00Z",
    "completedAt": "2024-08-15T15:00:00Z"
  }
}
```

**Testing Scenarios:**
1. **Valid Payment ID:** Existing payment → Should return 200 with details
2. **Invalid Payment ID:** Non-existent UUID → Should return 404 Not Found

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/payments/{paymentId}`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

### 20. Create Payment (with Invoice Validation)

**Endpoint:** `POST /api/payment/payments`

**Purpose:** Create a new payment with optional invoice validation.

**Authentication:** Requires `PAYMENT:CREATE` permission (JWT or API Key)

**Request:**
```json
{
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "PAYSTACK",
  "invoiceId": "550e8400-e29b-41d4-a716-446655440006"
}
```

**Request Details:**
- `amount`: Payment amount (required)
- `currency`: Currency code (required)
- `paymentMethod`: Payment method (CASH, PAYSTACK, etc.) (required)
- `invoiceId`: Optional invoice ID for validation (optional)

**Invoice Validation:**
When `invoiceId` is provided, the system validates:
- Invoice exists
- Payment amount does not exceed outstanding balance

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Payment created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440007",
    "paymentNumber": "PAY-2024-00001",
    "amount": 100.00,
    "currency": "USD",
    "status": "PENDING",
    "paymentMethod": "PAYSTACK",
    "invoiceId": "550e8400-e29b-41d4-a716-446655440006"
  }
}
```

**Error Response (400 Bad Request) - Payment Exceeds Balance:**
```json
{
  "type": "https://api.smartledger.com/errors/ERR-4001",
  "title": "Payment Exceeds Invoice Balance",
  "status": 400,
  "detail": "Payment amount 150.00 exceeds outstanding balance 100.00",
  "instance": "/api/payment/payments"
}
```

**Error Response (404 Not Found) - Invoice Not Found:**
```json
{
  "type": "https://api.smartledger.com/errors/ERR-3005",
  "title": "Not Found",
  "status": 404,
  "detail": "Invoice not found",
  "instance": "/api/payment/payments"
}
```

**Testing Scenarios:**
1. **Valid Payment without Invoice:** No invoiceId → Should return 201
2. **Valid Payment with Invoice:** Amount within balance → Should return 201
3. **Payment Exceeds Balance:** Amount > outstanding balance → Should return 400
4. **Non-existent Invoice:** Invalid invoiceId → Should return 404
5. **Invalid Payment Method:** Not a valid method → Should return 400

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/payment/payments`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>` OR `X-Service-Api-Key: <your-api-key>`
- Body: Raw JSON with payment details

**Important Notes:**
- Invoice validation prevents the "stranded student" scenario where money is taken by Paystack but rejected locally
- Validation happens BEFORE the payment gateway call
- This ensures students aren't charged for payments that would be rejected

---

### 21. Paystack Webhook

**Endpoint:** `POST /api/payment/webhook/paystack`

**Purpose:** Receive payment status updates from Paystack.

**Authentication:** No JWT required (uses HMAC signature validation)

**Security:** Webhook signature is validated using HMAC SHA512 with Paystack secret key.

**Request Headers:**
```
X-Paystack-Signature: <hmac-sha512-signature>
Content-Type: application/json
```

**Request Body (Paystack webhook payload):**
```json
{
  "event": "charge.success",
  "data": {
    "reference": "PAY-2024-00001",
    "amount": 10000,
    "currency": "USD",
    "status": "success",
    "paid_at": "2024-08-15T14:30:00Z"
  }
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Webhook processed successfully",
  "data": null
}
```

**Error Response (400 Bad Request) - Invalid Signature:**
```json
{
  "success": false,
  "message": "Invalid webhook signature",
  "data": null
}
```

**Testing Scenarios:**
1. **Valid Signature:** Correct HMAC signature → Should return 200
2. **Invalid Signature:** Wrong signature → Should return 400
3. **Missing Signature Header:** No signature header → Should return 400
4. **Invalid Payload:** Malformed JSON → Should return 400

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/payment/webhook/paystack`
- Headers: 
  - `Content-Type: application/json`
  - `X-Paystack-Signature: <computed-hmac-signature>`
- Body: Raw JSON with Paystack webhook payload

**Computing HMAC Signature:**
To test locally, compute the signature:
```bash
echo -n "<payload>" | openssl dgst -sha512 -hmac "<paystack-secret-key>"
```

**Important Notes:**
- This endpoint bypasses JWT authentication (permitAll in security config)
- Signature validation is performed by WebhookSignatureValidator
- Uses constant-time comparison to prevent timing attacks
- Paystack secret key is configured in application properties

---

### 22. Pay Fee Invoice with Payment Gateway (Automatic)

**Endpoint:** `POST /api/fees/invoices/{invoiceId}/pay-with-gateway`

**Purpose:** Automatically initiate payment for a fee invoice using payment gateway with automatic redirect.

**Authentication:** Requires `FEE:UPDATE` permission

**URL Parameters:**
- `invoiceId`: UUID of the fee invoice (path parameter)

**Request:**
```json
{
  "payerEmail": "student@example.com",
  "payerName": "John Doe",
  "payerPhone": "+2341234567890",
  "callbackUrl": "http://localhost:8081/api/payment/callback/paystack"
}
```

**Request Details:**
- `payerEmail`: Payer's email address (required)
- `payerName`: Payer's full name (optional)
- `payerPhone`: Payer's phone number (optional)
- `callbackUrl`: URL for Paystack to redirect after payment (required)

**Automatic Features:**
- Automatically fetches invoice details
- Uses invoice balance as payment amount
- Generates payment number automatically
- Initiates Paystack payment
- Returns authorization URL for redirect

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Payment initiated successfully. Redirect to authorizationUrl to complete payment.",
  "data": {
    "id": "550e8400-e29b-41d4-a16-446655440008",
    "paymentNumber": "PAY-1723456789-ABC12345",
    "paymentDate": "2024-08-15T14:30:00Z",
    "paymentMethod": "PAYSTACK",
    "amount": 5000.00,
    "currencyCode": "NGN",
    "payerName": "John Doe",
    "payerEmail": "student@example.com",
    "payerPhone": "+2341234567890",
    "description": "Fee payment for invoice: INV-24-00001",
    "status": "PENDING",
    "authorizationUrl": "https://checkout.paystack.com/xyz123abc",
    "accessCode": "xyz123abc",
    "gatewayReference": "paystack_ref_123",
    "callbackUrl": "http://localhost:8081/api/payment/callback/paystack",
    "invoiceId": "550e8400-e29b-41d4-a16-446655440006"
  }
}
```

**Error Response (404 Not Found) - Invoice Not Found:**
```json
{
  "success": false,
  "message": "Invoice not found",
  "data": null
}
```

**Testing Scenarios:**
1. **Valid Invoice:** Existing invoice with balance → Should return 201 with authorizationUrl
2. **Non-existent Invoice:** Invalid invoiceId → Should return 404
3. **Missing Email:** No payerEmail → Should return 400
4. **Zero Balance:** Invoice already paid → Should return 400

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/fees/invoices/{invoiceId}/pay-with-gateway`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with payer details

**Complete Flow:**
1. Call this endpoint with invoice ID
2. Receive authorizationUrl in response
3. Redirect user to authorizationUrl (Paystack checkout)
4. User completes payment on Paystack
5. Paystack redirects to callbackUrl
6. Callback verifies payment and updates status

**Important Notes:**
- This is the recommended endpoint for fee payments
- Automatically handles invoice balance validation
- Generates unique payment number if not provided
- Links payment to invoice automatically
- Returns accessCode for certain payment flows

---

### 23. Initiate Generic Gateway Payment

**Endpoint:** `POST /api/payment/initiate-gateway-payment`

**Purpose:** Initiate a payment using payment gateway for any payment type (not just fees).

**Authentication:** Requires `PAYMENT:CREATE` permission

**Request:**
```json
{
  "paymentNumber": "PAY-2024-00001",
  "paymentDate": "2024-08-15T14:30:00Z",
  "paymentMethod": "PAYSTACK",
  "amount": 10000.00,
  "currencyCode": "NGN",
  "payerEmail": "payer@example.com",
  "payerName": "Jane Smith",
  "payerPhone": "+2349876543210",
  "description": "General payment",
  "callbackUrl": "http://localhost:8081/api/payment/callback/paystack"
}
```

**Request Details:**
- `paymentNumber`: Unique payment identifier (optional, auto-generated if empty)
- `paymentDate`: Payment date/time (required)
- `paymentMethod`: Payment method - must be PAYSTACK for gateway (required)
- `amount`: Payment amount in base currency (required)
- `currencyCode`: Currency code (required)
- `payerEmail`: Payer's email address (required)
- `payerName`: Payer's full name (optional)
- `payerPhone`: Payer's phone number (optional)
- `description`: Payment description (optional)
- `callbackUrl`: URL for Paystack redirect (required)

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Payment initiated successfully.Redirect to authorizationUrl to complete payment.",
  "data": {
    "id": "550e8400-e29b-41d4-a16-446655440009",
    "paymentNumber": "PAY-2024-00001",
    "paymentDate": "2024-08-15T14:30:00Z",
    "paymentMethod": "PAYSTACK",
    "amount": 10000.00,
    "currencyCode": "NGN",
    "payerName": "Jane Smith",
    "payerEmail": "payer@example.com",
    "payerPhone": "+2349876543210",
    "description": "General payment",
    "status": "PENDING",
    "authorizationUrl": "https://checkout.paystack.com/abc456def",
    "accessCode": "abc456def",
    "gatewayReference": "paystack_ref_456",
    "callbackUrl": "http://localhost:8081/api/payment/callback/paystack"
  }
}
```

**Testing Scenarios:**
1. **Valid Payment:** All fields correct → Should return 201 with authorizationUrl
2. **Missing Required Fields:** Missing amount or email → Should return 400
3. **Invalid Payment Method:** Not PAYSTACK → Should return 400
4. **Negative Amount:** Amount less than 0 → Should return 400

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/payment/initiate-gateway-payment`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with payment details

**Use Cases:**
- General payments not linked to invoices
- Donations
- One-time payments
- Custom payment amounts
- Testing payment gateway integration

**Important Notes:**
- Use this endpoint for non-fee payments
- Payment number auto-generated if not provided
- Supports optional invoice linking via invoiceId field
- Returns same response structure as fee payment endpoint

---

### 24. Verify Payment with Gateway

**Endpoint:** `GET /api/payment/verify/{reference}`

**Purpose:** Verify a payment with the payment gateway using its reference and retrieve detailed payment information.

**Authentication:** Requires `PAYMENT:READ` permission

**URL Parameters:**
- `reference`: Payment reference from payment gateway (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment verified",
  "data": {
    "status": true,
    "message": "Verification successful",
    "data": {
      "reference": "paystack_ref_123",
      "gatewayResponse": "Successful",
      "paidAt": "2024-08-15T14:35:00Z",
      "createdAt": "2024-08-15T14:30:00Z",
      "channel": "card",
      "currency": "NGN",
      "amount": "10000",
      "metadata": {
        "custom_field": "value"
      },
      "customer": {
        "email": "payer@example.com",
        "customerCode": "CUS_abc123"
      },
      "authorization": {
        "authorizationCode": "AUTH_123abc",
        "bin": "408408",
        "last4": "4084",
        "expMonth": "12",
        "expYear": "2025",
        "cardType": "visa debit",
        "bank": "Guaranty Trust Bank"
      }
    }
  }
}
```

**Error Response (400 Bad Request) - Verification Failed:**
```json
{
  "success": true,
  "message": "Payment verification failed",
  "data": {
    "status": false,
    "message": "Payment not found or failed",
    "data": null
  }
}
```

**Error Response (404 Not Found) - Payment Not Found:**
```json
{
  "success": false,
  "message": "Payment with reference paystack_ref_123 not found",
  "data": null
}
```

**Testing Scenarios:**
1. **Valid Reference:** Existing payment → Should return 200 with full details
2. **Invalid Reference:** Non-existent reference → Should return 404
3. **Failed Payment:** Payment failed on gateway → Should return 200 with status=false
4. **Pending Payment:** Payment still processing → Should return 200 with status=false

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/payment/verify/{reference}`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

**Important Notes:**
- Automatically completes payment if verification succeeds
- Automatically fails payment if verification fails
- Returns detailed customer and authorization information
- Useful for backend testing and reconciliation
- Can be called manually if webhook fails

---

### 25. Paystack Payment Callback (Enhanced)

**Endpoint:** `GET /api/payment/callback/paystack`

**Purpose:** Handle Paystack payment callback after user completes payment and trigger automatic verification.

**Authentication:** No JWT required (public endpoint)

**Query Parameters:**
- `reference`: Payment reference from Paystack (optional)
- `trxref`: Alternative reference parameter (optional)

**Success Response (200 OK) - Payment Verified:**
```json
{
  "success": true,
  "message": "Payment verified",
  "data": {
    "reference": "paystack_ref_123",
    "message": "Payment verified and processed successfully",
    "status": "verified"
  }
}
```

**Success Response (200 OK) - Payment Not Found:**
```json
{
  "success": true,
  "message": "Callback received",
  "data": {
    "reference": "paystack_ref_123",
    "message": "Payment callback received but payment not found. Status will be updated via webhook.",
    "status": "pending_webhook"
  }
}
```

**Success Response (200 OK) - Verification Failed:**
```json
{
  "success": true,
  "message": "Callback received with error",
  "data": {
    "reference": "paystack_ref_123",
    "message": "Payment callback received but verification failed. Status will be updated via webhook.",
    "status": "verification_failed"
  }
}
```

**Testing Scenarios:**
1. **Valid Reference:** Payment exists → Should verify and complete payment
2. **Invalid Reference:** Payment doesn't exist → Should acknowledge and wait for webhook
3. **No Reference:** Missing parameters → Should return error message
4. **Verification Error:** Gateway error → Should acknowledge and wait for webhook

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/payment/callback/paystack?reference=paystack_ref_123`
- Headers: None required (public endpoint)

**Important Notes:**
- Enhanced to automatically trigger payment verification
- Falls back to webhook processing if verification fails
- Always returns 200 to acknowledge callback (Paystack requirement)
- Prevents duplicate payment completion via idempotency
- User-friendly redirect after payment completion

---

## Payment Gateway Integration Flow

### Complete Payment Flow with Automatic Redirect

```
1. User initiates payment (Fee or Generic)
   ↓
2. Backend creates payment record
   ↓
3. Backend calls Paystack initialize API
   ↓
4. Paystack returns authorizationUrl and accessCode
   ↓
5. Frontend receives authorizationUrl
   ↓
6. User redirected to Paystack checkout
   ↓
7. User enters payment details on Paystack
   ↓
8. Paystack processes payment
   ↓
9. Paystack redirects to callbackUrl
   ↓
10. Callback endpoint triggers verification
   ↓
11. Verification calls Paystack verify API
   ↓
12. Payment status updated (COMPLETED/FAILED)
   ↓
13. User redirected to dashboard
   ↓
14. Webhook also processes (backup mechanism)
```

### Fee Payment Flow (Recommended)

```
POST /api/fees/invoices/{invoiceId}/pay-with-gateway
{
  "payerEmail": "student@example.com",
  "payerName": "John Doe",
  "payerPhone": "+2341234567890",
  "callbackUrl": "http://localhost:8081/api/payment/callback/paystack"
}
↓
Response: { "authorizationUrl": "https://checkout.paystack.com/..." }
↓
Redirect user to authorizationUrl
↓
User completes payment on Paystack
↓
Paystack redirects to callbackUrl
↓
Callback verifies and completes payment
```

### Generic Payment Flow

```
POST /api/payment/initiate-gateway-payment
{
  "paymentNumber": "PAY-2024-00001",
  "paymentDate": "2024-08-15T14:30:00Z",
  "paymentMethod": "PAYSTACK",
  "amount": 10000.00,
  "currencyCode": "NGN",
  "payerEmail": "payer@example.com",
  "payerName": "Jane Smith",
  "callbackUrl": "http://localhost:8081/api/payment/callback/paystack"
}
↓
Response: { "authorizationUrl": "https://checkout.paystack.com/..." }
↓
Redirect user to authorizationUrl
↓
User completes payment on Paystack
↓
Paystack redirects to callbackUrl
↓
Callback verifies and completes payment
```

### Manual Verification Flow (Backend Testing)

```
GET /api/payment/verify/{reference}
↓
Response: { "status": true, "data": { ...customer details, authorization details... } }
↓
Payment automatically completed if verification succeeds
```

---

## Payment Gateway Configuration

### Required Configuration Properties

```yaml
payment:
  gateway:
    paystack:
      secret-key: ${PAYSTACK_SECRET_KEY:}
      public-key: ${PAYSTACK_PUBLIC_KEY:}
      callback-url: ${PAYSTACK_CALLBACK_URL:http://localhost:8081/api/payment/callback/paystack}
      webhook-url: ${PAYSTACK_WEBHOOK_URL:http://localhost:8081/api/payment/webhook/paystack}
      api-url: ${PAYSTACK_API_URL:https://api.paystack.co}
```

### Environment Variables

- `PAYSTACK_SECRET_KEY`: Your Paystack secret key (required for production)
- `PAYSTACK_PUBLIC_KEY`: Your Paystack public key (required for frontend)
- `PAYSTACK_CALLBACK_URL`: URL for Paystack callbacks (optional, defaults to local)
- `PAYSTACK_WEBHOOK_URL`: URL for webhook events (optional, defaults to local)
- `PAYSTACK_API_URL`: Paystack API base URL (optional, defaults to production)

### Testing with Paystack Test Mode

1. Get test keys from Paystack dashboard
2. Set environment variables with test keys
3. Use test card numbers for payments:
   - Success: `4084084084084081`
   - Insufficient funds: `4084084084084081`
   - Wrong PIN: `5339 5339 5339 5339`

---

## Payment Gateway Testing Scenarios

### Scenario 1: Complete Fee Payment Flow

**Steps:**
1. Create fee invoice: `POST /api/fees/invoices`
2. Pay with gateway: `POST /api/fees/invoices/{invoiceId}/pay-with-gateway`
3. Get authorizationUrl from response
4. Manually visit authorizationUrl in browser (or use for frontend testing)
5. Complete payment on Paystack test page
6. Verify payment: `GET /api/payment/verify/{reference}`
7. Check payment status: `GET /api/payment/payments/{paymentId}`

**Expected Result:** Payment status should be COMPLETED

### Scenario 2: Generic Payment Flow

**Steps:**
1. Initiate gateway payment: `POST /api/payment/initiate-gateway-payment`
2. Get authorizationUrl from response
3. Visit authorizationUrl and complete payment
4. Verify payment: `GET /api/payment/verify/{reference}`
5. Check payment status: `GET /api/payment/payments/{paymentId}`

**Expected Result:** Payment status should be COMPLETED

### Scenario 3: Payment Verification

**Steps:**
1. Create payment via gateway
2. Get reference from response
3. Call verification: `GET /api/payment/verify/{reference}`
4. Check response includes customer and authorization details

**Expected Result:** Full payment details including customer info, card details, and authorization code

### Scenario 4: Callback Handling

**Steps:**
1. Create payment via gateway
2. Simulate Paystack callback: `GET /api/payment/callback/paystack?reference={ref}`
3. Check payment status

**Expected Result:** Payment automatically verified and completed

### Scenario 5: Webhook Processing

**Steps:**
1. Create payment via gateway
2. Simulate Paystack webhook: `POST /api/payment/webhook/paystack`
3. Include proper HMAC signature
4. Check payment status

**Expected Result:** Payment status updated based on webhook event

---

## Payment Gateway Error Handling

### Common Errors and Solutions

**Error: "Payment amount exceeds outstanding balance"**
- Cause: Payment amount greater than invoice balance
- Solution: Reduce payment amount or pay full balance

**Error: "Invalid webhook signature"**
- Cause: HMAC signature doesn't match
- Solution: Ensure correct secret key and signature computation

**Error: "Payment with reference not found"**
- Cause: Reference doesn't match any payment
- Solution: Verify reference is correct from payment initiation response

**Error: "Failed to initiate payment with Paystack"**
- Cause: Paystack API error or invalid credentials
- Solution: Check secret key, API URL, and network connectivity

---

## Ledger/Account Management

### 22. Create Account

**Endpoint:** `POST /api/v1/accounts`

**Purpose:** Create a new ledger account.

**Authentication:** Requires `LEDGER:UPDATE` permission

**Request:**
```json
{
  "accountNumber": "1001",
  "accountCode": "AST",
  "accountName": "Cash",
  "accountType": "ASSET",
  "normalBalance": "DEBIT",
  "currency": "USD",
  "openingBalance": 10000.00,
  "description": "Primary cash account"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440008",
    "accountNumber": "1001",
    "accountCode": "AST",
    "accountName": "Cash",
    "accountType": "ASSET",
    "normalBalance": "DEBIT",
    "currentBalance": 10000.00,
    "currency": "USD",
    "isActive": true
  }
}
```

**Testing Scenarios:**
1. **Valid Account Creation:** All fields correct → Should return 201
2. **Duplicate Account Number:** Number already exists → Should return 409 Conflict
3. **Invalid Account Type:** Not a valid type → Should return 400 Bad Request
4. **Invalid Normal Balance:** Not DEBIT or CREDIT → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/accounts`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with account details

---

### 23. Get Account Balance

**Endpoint:** `GET /api/v1/accounts/{accountId}/balance`

**Purpose:** Retrieve current balance of an account.

**Authentication:** Requires `LEDGER:READ` permission

**URL Parameters:**
- `accountId`: UUID of the account (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Balance retrieved successfully",
  "data": {
    "accountId": "550e8400-e29b-41d4-a716-446655440008",
    "accountNumber": "1001",
    "accountName": "Cash",
    "currentBalance": 15000.00,
    "currency": "USD",
    "lastUpdated": "2024-08-15T16:00:00Z"
  }
}
```

**Testing Scenarios:**
1. **Valid Account ID:** Existing account → Should return 200 with balance
2. **Invalid Account ID:** Non-existent UUID → Should return 404 Not Found

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/accounts/{accountId}/balance`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

## Journal Entries

### 24. Create Journal Entry

**Endpoint:** `POST /api/v1/journal-entries`

**Purpose:** Create a new journal entry for double-entry bookkeeping.

**Authentication:** Requires `JOURNAL:CREATE` permission

**Request:**
```json
{
  "entryDate": "2024-08-15",
  "description": "Tuition fee payment received",
  "reference": "INV-24-00001",
  "lines": [
    {
      "accountId": "550e8400-e29b-41d4-a716-446655440008",
      "debitAmount": 2500.00,
      "creditAmount": 0.00,
      "description": "Cash received"
    },
    {
      "accountId": "550e8400-e29b-41d4-a716-446655440009",
      "debitAmount": 0.00,
      "creditAmount": 2500.00,
      "description": "Tuition revenue"
    }
  ]
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Journal entry created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440010",
    "entryNumber": "JE-2024-00001",
    "entryDate": "2024-08-15",
    "status": "POSTED",
    "totalDebit": 2500.00,
    "totalCredit": 2500.00,
    "lines": [
      {
        "accountId": "550e8400-e29b-41d4-a716-446655440008",
        "debitAmount": 2500.00,
        "creditAmount": 0.00
      },
      {
        "accountId": "550e8400-e29b-41d4-a716-446655440009",
        "debitAmount": 0.00,
        "creditAmount": 2500.00
      }
    ]
  }
}
```

**Testing Scenarios:**
1. **Valid Journal Entry:** Debits equal credits → Should return 201
2. **Unbalanced Entry:** Debits don't equal credits → Should return 400 Bad Request
3. **Invalid Account ID:** Account doesn't exist → Should return 404 Not Found
4. **Missing Lines:** No journal lines provided → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/journal-entries`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with journal entry details

---

## Reporting

### 25. Generate Balance Sheet

**Endpoint:** `GET /api/v1/reports/balance-sheet`

**Purpose:** Generate balance sheet report as of a specific date.

**Authentication:** Requires `REPORT:GENERATE` permission

**Query Parameters:**
- `asOfDate`: Date for balance sheet (required, format: YYYY-MM-DD)

**Example Request:**
```
GET /api/v1/reports/balance-sheet?asOfDate=2024-08-15
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Balance sheet generated successfully",
  "data": {
    "asOfDate": "2024-08-15",
    "assets": {
      "currentAssets": 50000.00,
      "fixedAssets": 150000.00,
      "totalAssets": 200000.00
    },
    "liabilities": {
      "currentLiabilities": 30000.00,
      "longTermLiabilities": 50000.00,
      "totalLiabilities": 80000.00
    },
    "equity": {
      "shareCapital": 100000.00,
      "retainedEarnings": 20000.00,
      "totalEquity": 120000.00
    },
    "totalLiabilitiesAndEquity": 200000.00
  }
}
```

**Testing Scenarios:**
1. **Valid Date:** Correct date format → Should return 200 with balance sheet
2. **Invalid Date Format:** Malformed date → Should return 400 Bad Request
3. **Future Date:** Date in the future → Should return 400 Bad Request

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/reports/balance-sheet?asOfDate=2024-08-15`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

### 23. Generate Income Statement

**Endpoint:** `GET /api/v1/reports/income-statement`

**Purpose:** Generate income statement for a specific period.

**Authentication:** Requires `REPORT:GENERATE` permission

**Query Parameters:**
- `startDate`: Period start date (required, format: YYYY-MM-DD)
- `endDate`: Period end date (required, format: YYYY-MM-DD)

**Example Request:**
```
GET /api/v1/reports/income-statement?startDate=2024-01-01&endDate=2024-08-15
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Income statement generated successfully",
  "data": {
    "period": {
      "startDate": "2024-01-01",
      "endDate": "2024-08-15"
    },
    "revenue": {
      "tuitionFees": 100000.00,
      "otherRevenue": 5000.00,
      "totalRevenue": 105000.00
    },
    "expenses": {
      "salaries": 40000.00,
      "utilities": 10000.00,
      "otherExpenses": 5000.00,
      "totalExpenses": 55000.00
    },
    "netIncome": 50000.00
  }
}
```

**Testing Scenarios:**
1. **Valid Period:** Correct date range → Should return 200 with income statement
2. **Invalid Date Format:** Malformed dates → Should return 400 Bad Request
3. **End Before Start:** End date before start date → Should return 400 Bad Request

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/reports/income-statement?startDate=2024-01-01&endDate=2024-08-15`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

## AI Insights

### 24. Trigger AI Analysis

**Endpoint:** `POST /api/v1/ai-insights/analyze`

**Purpose:** Trigger AI-powered financial analysis and anomaly detection.

**Authentication:** Requires appropriate permissions

**Request:**
```json
{
  "analysisType": "ANOMALY_DETECTION",
  "parameters": {
    "timeframe": "LAST_30_DAYS",
    "accounts": ["all"],
    "threshold": 0.95
  }
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "AI analysis initiated",
  "data": {
    "analysisId": "550e8400-e29b-41d4-a716-446655440011",
    "status": "IN_PROGRESS",
    "estimatedCompletionTime": "2024-08-15T17:00:00Z"
  }
}
```

**Testing Scenarios:**
1. **Valid Analysis Request:** All parameters correct → Should return 200
2. **Invalid Analysis Type:** Not a valid type → Should return 400 Bad Request
3. **Invalid Timeframe:** Not a valid timeframe → Should return 400 Bad Request

**Postman Setup:**
- Method: POST
- URL: `http://localhost:8081/api/v1/ai-insights/analyze`
- Headers: 
  - `Content-Type: application/json`
  - `Authorization: Bearer <your-jwt-token>`
- Body: Raw JSON with analysis parameters

**Important Notes:**
- Requires n8n integration to be configured
- Analysis runs asynchronously
- Results can be retrieved via the analysis ID

---

### 25. Get AI Insights

**Endpoint:** `GET /api/v1/ai-insights/{analysisId}`

**Purpose:** Retrieve results of a completed AI analysis.

**Authentication:** Requires appropriate permissions

**URL Parameters:**
- `analysisId`: UUID of the analysis (path parameter)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "AI insights retrieved successfully",
  "data": {
    "analysisId": "550e8400-e29b-41d4-a716-446655440011",
    "status": "COMPLETED",
    "anomalies": [
      {
        "type": "UNUSUAL_SPENDING",
        "severity": "HIGH",
        "description": "Unusual spending pattern detected in utilities",
        "affectedAccounts": ["2001"],
        "recommendedActions": ["Review utility bills", "Verify meter readings"]
      }
    ],
    "insights": [
      {
        "type": "CASH_FLOW_PREDICTION",
        "prediction": "POSITIVE",
        "confidence": 0.87,
        "description": "Cash flow expected to remain positive for next 30 days"
      }
    ],
    "completedAt": "2024-08-15T17:00:00Z"
  }
}
```

**Testing Scenarios:**
1. **Completed Analysis:** Analysis finished → Should return 200 with insights
2. **In Progress Analysis:** Analysis still running → Should return 202 Accepted
3. **Invalid Analysis ID:** Non-existent UUID → Should return 404 Not Found

**Postman Setup:**
- Method: GET
- URL: `http://localhost:8081/api/v1/ai-insights/{analysisId}`
- Headers: 
  - `Authorization: Bearer <your-jwt-token>`

---

## Error Response Format

All endpoints follow the RFC 7807 Problem Details for HTTP APIs format for errors:

```json
{
  "type": "https://api.smartledger.com/errors/ERR-XXXX",
  "title": "Error Title",
  "status": 400,
  "detail": "Detailed error message",
  "instance": "/api/v1/endpoint"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required or failed
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (duplicate, etc.)
- `500 Internal Server Error` - Server error

---

## Testing Best Practices

### 1. Authentication Flow
- Always start with login to get JWT token
- Include token in Authorization header for protected endpoints
- Test token expiration by waiting for expiry or using expired token
- Test permission-based access control with different user roles

### 2. Error Handling
- Test all error scenarios (invalid data, missing fields, etc.)
- Verify error response format matches RFC 7807
- Check that error messages are descriptive but not revealing

### 3. Data Validation
- Test with invalid data formats (malformed UUIDs, invalid dates, etc.)
- Test boundary conditions (negative amounts, past dates, etc.)
- Test required field validation (missing mandatory fields)

### 4. Business Logic
- Test business rules (e.g., debits must equal credits in journal entries)
- Test state transitions (e.g., DRAFT → ACTIVE → COMPLETED)
- Test idempotency where applicable

### 5. Security
- Test authorization with different permission levels
- Test that users can only access their own data where applicable
- Test that sensitive operations require appropriate permissions

---

## Postman Collection Structure

Recommended structure for organizing your Postman collection:

```
SmartLedger API Collection
├── Authentication
│   ├── Login
│   ├── Forgot Password
│   └── Reset Password
├── User Management
│   ├── Create User
│   ├── Get User
│   ├── Update User
│   ├── Change Password
│   ├── Grant Role
│   └── Revoke Role
├── Fee Management
│   ├── Create Schedule
│   ├── Add Fee Item
│   ├── Activate Schedule
│   ├── Create Invoice
│   ├── Generate Invoice from Schedule
│   ├── Record Payment
│   └── Complete Payment
├── Payment Management
│   ├── Get All Payments
│   └── Get Payment by ID
├── Ledger Management
│   ├── Create Account
│   └── Get Account Balance
├── Journal Entries
│   └── Create Journal Entry
├── Reporting
│   ├── Balance Sheet
│   └── Income Statement
└── AI Insights
    ├── Trigger Analysis
    └── Get Insights
```

---

## Environment Variables

Set up these environment variables in Postman:

```
base_url = http://localhost:8081
jwt_token = {{login_response.data.token}}
admin_username = admin
admin_password = admin
```

Use `{{base_url}}` in your request URLs and `{{jwt_token}}` in your Authorization headers.

---

## Quick Start Testing Sequence

1. **Login to get token**
   ```
   POST {{base_url}}/api/v1/auth/login
   Body: {"username": "{{admin_username}}", "password": "{{admin_password}}"}
   ```

2. **Create a user**
   ```
   POST {{base_url}}/api/v1/users
   Headers: Authorization: Bearer {{jwt_token}}
   Body: {"username": "testuser", "email": "test@example.com", "password": "Test123", "firstName": "Test", "lastName": "User"}
   ```

3. **Create a fee schedule**
   ```
   POST {{base_url}}/api/fees/schedules
   Headers: Authorization: Bearer {{jwt_token}}
   Body: {"code": "FEE-TEST-01", "name": "Test Schedule", "academicYear": "2024-2025", "term": "Term 1", "grade": "Grade 10", "effectiveDate": "2024-09-01", "expiryDate": "2025-08-31"}
   ```

4. **Add fee item to schedule**
   ```
   POST {{base_url}}/api/fees/schedules/{scheduleId}/items
   Headers: Authorization: Bearer {{jwt_token}}
   Body: {"feeType": "TUITION_FEE", "amount": 5000, "currency": "USD", "mandatory": true, "name": "Tuition"}
   ```

5. **Activate schedule**
   ```
   POST {{base_url}}/api/fees/schedules/{scheduleId}/activate
   Headers: Authorization: Bearer {{jwt_token}}
   Body: {"activatedBy": "admin"}
   ```

6. **Generate invoice from schedule**
   ```
   POST {{base_url}}/api/fees/invoices/generate-from-schedule
   Headers: Authorization: Bearer {{jwt_token}}
   Body: {"studentId": "{studentId}", "scheduleCode": "FEE-TEST-01", "dueDate": "2024-09-30"}
   ```

7. **Record payment**
   ```
   POST {{base_url}}/api/fees/invoices/{invoiceId}/payments
   Headers: Authorization: Bearer {{jwt_token}}
   Body: {"feeType": "TUITION_FEE", "amount": 2500, "currency": "USD", "paymentMethod": "CASH", "reference": "TEST-001"}
   ```

8. **Complete payment**
   ```
   POST {{base_url}}/api/fees/payments/{paymentId}/complete
   Headers: Authorization: Bearer {{jwt_token}}
   Body: {"receiptNumber": "TEST-001-CONFIRMED"}
   ```

---

## Notes

- All dates should be in ISO 8601 format (YYYY-MM-DD)
- All monetary amounts should be decimal numbers with 2 decimal places
- All currency codes should be ISO 4217 compliant (e.g., USD, EUR, GBP)
- UUIDs should be in standard format (e.g., 550e8400-e29b-41d4-a716-446655440000)
- The API uses RFC 7807 Problem Details for error responses
- JWT tokens expire based on configuration (check application properties)
- Some endpoints may require additional configuration (email service, n8n, etc.)

---

## Support

For issues or questions:
1. Check application logs for detailed error messages
2. Verify all required environment variables are set
3. Ensure database migrations have been applied
4. Check that required services (PostgreSQL, Redis, n8n) are running
5. Review this documentation for correct request formats
