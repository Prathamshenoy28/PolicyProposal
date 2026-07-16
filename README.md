# Policy Proposal Processing API

## Overview

This Spring Boot application implements a simplified insurance policy proposal system. It provides REST APIs for managing customers, creating policy proposals, and tracking audit records. All data is stored in-memory using Java Collections.

## Technology Stack

- **Java**: 17+
- **Framework**: Spring Boot 3.1.0
- **Build Tool**: Maven
- **Storage**: Java Collections (In-Memory)
- **Testing**: JUnit 5
- **API Testing**: Postman / cURL

## Project Structure

```
src/
├── main/
│   └── java/com/example/demo/
│       ├── controller/          # REST Controllers
│       ├── service/             # Business Logic
│       ├── repository/          # Data Access Layer
│       ├── model/               # Entity Models
│       ├── dto/                 # Data Transfer Objects
│       ├── exception/           # Custom Exceptions & Global Exception Handler
│       └── DemoApplication.java # Main Application Class
└── test/
    └── java/com/example/demo/
        └── service/             # Unit Tests
```

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build and Run

1. **Clone/Download the project**
   ```bash
   cd demo
   ```

2. **Build the project**
   ```bash
   mvn clean build
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Endpoints

### Reference Master
- **GET** `/reference-master/{category}`
  - Returns predefined reference data (POLICY_TERM, PAYMENT_FREQUENCY)
  - Categories: `POLICY_TERM`, `PAYMENT_FREQUENCY`

### Customer Management

- **POST** `/customers`
  - Creates a new customer
  - Request Body:
    ```json
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phone": "9999999999",
      "age": 35,
      "pan": "ABCDE1234F"
    }
    ```

- **GET** `/customers`
  - Returns the list of all customers

- **GET** `/customers/{id}`
  - Returns details of a specific customer

- **PUT** `/customers/{id}`
  - Updates an existing customer
  - Request Body: Same as POST

### Proposal Management

- **POST** `/proposals`
  - Creates a new policy proposal
  - Request Body:
    ```json
    {
      "customerId": "uuid",
      "policyTerm": 15,
      "sumAssured": 500000,
      "annualPremium": 10000,
      "paymentFrequency": "MONTHLY",
      "nomineeName": "Jane Doe"
    }
    ```

- **GET** `/proposals/{id}`
  - Returns details of a specific proposal

- **GET** `/proposals`
  - Returns all proposals

- **POST** `/proposals/{id}/submit`
  - Submits a proposal for processing
  - Generates unique policy number
  - Creates audit record

### Audit Management

- **GET** `/audits`
  - Returns all audit records

- **GET** `/audits/{entityId}`
  - Returns audit records for a specific entity

## Business Rules & Validations

### Customer Validation
- **Age**: 18 to 65 years (inclusive)
- **Email**: Valid email format
- **Phone**: 10 digits
- **Unique Email**: Each customer must have a unique email

### Proposal Validation
- **Policy Term**: 10, 15, 20, 25, or 30 years
- **Sum Assured**: Rs. 1,00,000 to Rs. 5,00,00,000
- **Annual Premium**: Minimum Rs. 5,000
- **PAN**: Mandatory if Annual Premium exceeds Rs. 50,000
- **Payment Frequency**: MONTHLY, QUARTERLY, HALF_YEARLY, or ANNUAL
- **Nominee**: Mandatory and cannot be the same as the customer

## Sample API Requests and Responses

### 1. Get Reference Data
```bash
curl -X GET http://localhost:8080/reference-master/POLICY_TERM
```

Response:
```json
[
  "10",
  "15",
  "20",
  "25",
  "30"
]
```

### 2. Create Customer
```bash
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phone": "9999999999",
    "age": 35,
    "pan": "ABCDE1234F"
  }'
```

Response:
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "9999999999",
  "age": 35,
  "pan": "ABCDE1234F",
  "createdAt": 1689000000000,
  "updatedAt": 1689000000000
}
```

### 3. Create Proposal
```bash
curl -X POST http://localhost:8080/proposals \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "policyTerm": 15,
    "sumAssured": 500000,
    "annualPremium": 10000,
    "paymentFrequency": "MONTHLY",
    "nomineeName": "Jane Doe"
  }'
```

Response:
```json
{
  "proposalId": "660e8400-e29b-41d4-a716-446655440001",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "policyTerm": 15,
  "sumAssured": 500000,
  "annualPremium": 10000,
  "paymentFrequency": "MONTHLY",
  "nomineeName": "Jane Doe",
  "status": "DRAFT",
  "policyNumber": null,
  "createdAt": 1689000001000,
  "submittedAt": null
}
```

### 4. Submit Proposal
```bash
curl -X POST http://localhost:8080/proposals/660e8400-e29b-41d4-a716-446655440001/submit
```

Response:
```json
{
  "proposalId": "660e8400-e29b-41d4-a716-446655440001",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "policyTerm": 15,
  "sumAssured": 500000,
  "annualPremium": 10000,
  "paymentFrequency": "MONTHLY",
  "nomineeName": "Jane Doe",
  "status": "SUBMITTED",
  "policyNumber": "POL1689000002000ABC12345",
  "createdAt": 1689000001000,
  "submittedAt": 1689000002000
}
```

### 5. Get Audits
```bash
curl -X GET http://localhost:8080/audits
```

Response:
```json
[
  {
    "auditId": "770e8400-e29b-41d4-a716-446655440002",
    "entityType": "CUSTOMER",
    "entityId": "550e8400-e29b-41d4-a716-446655440000",
    "action": "CREATE",
    "details": "Customer created: John Doe",
    "timestamp": 1689000000000
  },
  {
    "auditId": "770e8400-e29b-41d4-a716-446655440003",
    "entityType": "PROPOSAL",
    "entityId": "660e8400-e29b-41d4-a716-446655440001",
    "action": "CREATE",
    "details": "Proposal created for customer: 550e8400-e29b-41d4-a716-446655440000",
    "timestamp": 1689000001000
  }
]
```

## Error Handling

The API returns meaningful error responses with HTTP status codes:

- **400 Bad Request**: Validation errors or business rule violations
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected server errors

Error Response Example:
```json
{
  "status": 400,
  "message": "Customer age must be between 18 and 65 years",
  "timestamp": 1689000000000
}
```

## Running Tests

Execute unit tests:

```bash
mvn test
```

Tests are located in `src/test/java/com/example/demo/service/`

### Test Coverage

- **CustomerServiceTest**: Tests for customer creation, validation, updates, and retrieval
- **ProposalServiceTest**: Tests for proposal creation, submission, and all business validation rules

## Thread Safety

The application uses:
- `ConcurrentHashMap` for in-memory storage
- Synchronized blocks for critical operations
- Thread-safe operations for all repository methods

## Architecture

The application follows a layered architecture:

```
Controller Layer (REST Endpoints)
    ↓
Service Layer (Business Logic & Validations)
    ↓
Repository Layer (Data Access)
    ↓
In-Memory Storage (Java Collections)
```

### Exception Handling

Global exception handling is implemented via `GlobalExceptionHandler` which:
- Catches all exceptions uniformly
- Returns consistent error responses
- Maps exceptions to appropriate HTTP status codes
- Provides meaningful error messages

## Assumptions

1. Customer IDs and Proposal IDs are generated as UUID strings
2. Timestamps are stored in milliseconds (System.currentTimeMillis())
3. Policy numbers are generated as: `POL{timestamp}{uuid-substring}`
4. All data is lost when the application stops (in-memory storage)
5. Email uniqueness is enforced at the application level

## Notes

- All API responses include timestamps
- Audit records are created for every significant operation
- Input validation occurs at the API boundary (@Valid annotation)
- Business validations occur in the service layer
- No database is used; all data is stored in-memory
