# Policy Proposal Processing API

## Overview

This Spring Boot application implements a simplified insurance policy proposal system. It exposes REST APIs for creating and managing customers, creating and submitting policy proposals, reading reference data, and reviewing audit records.

The current implementation is a demo app that uses in-memory repositories, Spring field injection in the controllers and services, and direct domain-model responses from the controllers.

## Technology Stack

- Java 17+
- Spring Boot 3.1.0
- Maven
- JUnit 5
- Jakarta Validation
- Lombok

## Project Structure

```text
src/
├── main/
│   └── java/com/example/demo/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── dto/
│       └── exception/
└── test/
    └── java/com/example/demo/service/
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Run the app
From the project root:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The app will run on http://localhost:8080.

### Run tests

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

### Run one test class

```bash
./mvnw -Dtest=ProposalServiceTest test
```

## API Endpoints

### Reference data
- GET /reference-master/{category}
  - Returns predefined reference values such as POLICY_TERM and PAYMENT_FREQUENCY

### Customers
- POST /customers
  - Creates a new customer
- GET /customers
  - Returns all customers
- GET /customers/{id}
  - Returns one customer by ID
- PUT /customers/{id}
  - Updates a customer

### Proposals
- POST /proposals
  - Creates a new proposal in DRAFT state
- GET /proposals/{id}
  - Returns one proposal by ID
- POST /proposals/{id}/submit
  - Submits a proposal and generates a policy number
- GET /proposals
  - Returns all proposals

### Audits
- GET /audits
  - Returns all audit records
- GET /audits/{entityId}
  - Returns audit records for a specific entity

## Validation Rules

### Customer validation
- firstName and lastName are required
- email is required and must be valid
- phone must be exactly 10 digits
- age must be between 18 and 65

### Proposal validation
- customerId is required
- policyTerm must be one of 10, 15, 20, 25, 30
- sumAssured must be between Rs. 1,00,000 and Rs. 5,00,00,000
- annualPremium must be at least Rs. 5,000
- paymentFrequency must be one of MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL
- nomineeName is required and cannot match the customer name
- PAN is required when annual premium is above Rs. 50,000

## Sample cURL Requests

### 1. Get reference data
```bash
curl -X GET http://localhost:8080/reference-master/POLICY_TERM
```

### 2. Create a customer
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

Example response:
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

### 3. Create a proposal
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

Example response:
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

### 4. Submit the proposal
```bash
curl -X POST http://localhost:8080/proposals/660e8400-e29b-41d4-a716-446655440001/submit
```

Example response:
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
  "policyNumber": "POL1720000000000ABC12345",
  "createdAt": 1689000001000,
  "submittedAt": 1689000002000
}
```

### 5. View audit entries
```bash
curl -X GET http://localhost:8080/audits
```

## Error Handling

The API returns structured error responses for invalid requests or missing resources.

Example error response:
```json
{
  "status": 400,
  "message": "Customer age must be between 18 and 65 years",
  "timestamp": 1689000000000
}
```

Common status codes:
- 400 Bad Request: business-rule or validation failure
- 404 Not Found: entity not found
- 500 Internal Server Error: unexpected server error

## Notes

- Data is stored only in memory and is reset when the app stops.
- The app does not use a database.
- The current implementation uses field injection in the controllers and services.
- Tests are located under src/test/java/com/example/demo/service.
