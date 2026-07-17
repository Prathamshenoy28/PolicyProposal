# Design Decisions Document

## 1. Project Purpose

This project is a Spring Boot demo application for processing insurance policy proposals. It exposes REST endpoints for:
- creating and managing customers
- creating and submitting policy proposals
- reading reference data
- reviewing audit records

The app is intentionally lightweight and uses in-memory repositories so it can run without a database.

## 2. Current Architecture

The application follows a layered structure:

```text
Controller Layer -> Service Layer -> Repository Layer -> In-Memory Storage
```

### Layer responsibilities
- Controller layer: exposes REST endpoints and delegates to services
- Service layer: contains business rules and orchestration logic
- Repository layer: stores and retrieves domain objects in memory
- Model/DTO layer: defines the request and response objects used by the API

## 3. Storage and Data Model Decisions

### In-memory repositories
Decision: use simple Java collections in repository classes rather than a database.

Current implementation:
- CustomerRepository stores customers in a HashMap keyed by customerId
- ProposalRepository stores proposals in a HashMap keyed by proposalId
- AuditRepository stores audit entries in a HashMap keyed by auditId
- ReferenceDataRepository stores predefined reference values in a HashMap

Why this was chosen:
- keeps the application simple and easy to run locally
- avoids setup overhead for database configuration
- makes the project suitable for a demo or assignment

Trade-off:
- data is not persisted after application shutdown
- the current implementation is not designed for production-grade concurrency

## 4. ID and Number Generation

### Entity IDs
Decision: use UUID strings for customer, proposal, and audit IDs.

Why:
- avoids manual ID management
- keeps IDs unique across the in-memory store
- is simple to use in REST APIs

### Policy number generation
Decision: generate policy numbers as a string in the form:

```text
POL{timestamp}{uuid-substring}
```

Example:
```text
POL1720000000000ABC12345
```

## 5. Validation Strategy

The application uses a two-step validation approach.

### A. API boundary validation
The request DTOs use Bean Validation annotations from Jakarta Validation.

Examples:
- firstName, lastName, email, phone, and age are validated on customer requests
- proposal fields such as policyTerm, sumAssured, annualPremium, paymentFrequency, and nomineeName are validated on proposal requests

This ensures invalid input is rejected early.

### B. Business-rule validation
The service layer performs custom rules that require more context than DTO validation alone.

Current business rules include:
- customer age must be between 18 and 65
- each email must be unique
- policy term must be one of 10, 15, 20, 25, or 30
- sum assured must be between Rs. 1,00,000 and Rs. 5,00,00,000
- annual premium must be at least Rs. 5,000
- PAN is mandatory when annual premium exceeds Rs. 50,000
- nominee cannot be the same as the customer
- a proposal can only be submitted once

## 6. Dependency Injection Style

Decision: keep the current Spring field-injection style in controllers and services.

Current pattern:
```java
@Autowired
private CustomerService customerService;
```

Why this style is used here:
- it matches the existing code style in the project
- it keeps the implementation concise for this demo app
- it is easy to use in unit tests by assigning the dependencies directly

Note:
- constructor injection is generally preferred in larger production systems
- the current codebase intentionally uses field injection for consistency and simplicity

## 7. Exception Handling

Decision: centralize error handling with a global exception handler.

The application uses:
- BusinessException for business-rule violations
- ResourceNotFoundException for missing entities
- GlobalExceptionHandler to return consistent error responses

Error responses include:
- status code
- message
- timestamp

## 8. DTO and API Design

Decision: use dedicated DTOs for incoming request payloads and return the domain models directly from the controller layer.

Current DTOs:
- CustomerRequest
- ProposalRequest

Current response objects:
- Customer
- Proposal
- Audit

This keeps the API simple while still separating request input from domain entities.

## 9. Audit Trail Design

Decision: create audit records whenever significant actions occur.

Current audit events include:
- customer creation
- customer update
- proposal creation
- proposal submission

Each audit entry stores:
- audit ID
- entity type
- entity ID
- action
- details
- timestamp

## 10. Testing Strategy

The project uses JUnit 5 tests for the service layer.

Current test coverage includes:
- customer creation and update scenarios
- validation failures for customer input
- proposal creation and submission flows
- proposal validation rules
- duplicate or invalid business cases

The tests instantiate services directly and assign repository dependencies manually to match the current field-injection style.

## 11. Current Limitations

The current version is a demo implementation and has a few intentional simplifications:
- no database or persistence layer
- no authentication or authorization
- no pagination for large result sets
- no production-grade concurrency handling
- no external configuration for reference data

## 12. Verification Notes

The project has been verified with the Maven wrapper from the project root.

Example command:
```bash
./mvnw.cmd -Dtest=ProposalServiceTest test -q
```

The proposal service test suite currently reports 10 tests run with 0 failures and 0 errors.

## Trade-offs and Considerations

### 1. Scalability vs Simplicity

**Decision:** Chose simplicity with in-memory storage

**Trade-off:**
- ✓ Simple to implement and understand
- ✗ Cannot scale beyond single JVM
- ✗ Data loss on restart
- Mitigated by: Explicit requirement for in-memory storage

### 2. Type Safety vs Flexibility

**Decision:** Strong typing with specific request/response DTOs

**Trade-off:**
- ✓ Compile-time safety
- ✓ Clear API contracts
- ✗ More verbose code
- Mitigated by: Using Lombok to reduce boilerplate

### 3. Validation Location

**Decision:** Split validation between API layer and service layer

**Trade-off:**
- ✓ Early failure for basic validation
- ✓ Business logic separated from framework
- ✗ Slightly more complex code
- Mitigated by: Clear separation and documentation

### 4. Synchronization Strategy

**Decision:** ConcurrentHashMap + explicit synchronization

**Trade-off:**
- ✓ Good performance for read-heavy workloads
- ✓ Thread-safe compound operations
- ✗ Some synchronization overhead
- Mitigated by: Only synchronize when necessary

## Future Enhancements

### 1. Database Integration
- Replace ConcurrentHashMap with JPA/Hibernate
- Add Spring Data repositories
- Maintain same service/controller layer

### 2. Additional Features
- DELETE endpoints for customers and proposals
- PII masking in customer responses
- Pagination for list endpoints
- Audit lookup by entity ID
- Request logging
- Caching layer

### 3. Security
- Authentication (OAuth2/JWT)
- Authorization (role-based access)
- HTTPS/TLS
- Input sanitization for security

### 4. Performance
- Caching layer (Spring Cache)
- Batch operations
- Database indexing
- Query optimization

### 5. Monitoring
- Logging (SLF4J)
- Metrics (Micrometer)
- Health checks
- Error tracking

### 6. Documentation
- OpenAPI/Swagger integration
- API documentation generation
- Interactive API testing

## Assumptions Made

1. **ID Generation**: All IDs are generated as UUIDs (not sequential)
2. **Timestamps**: Stored as milliseconds since epoch (System.currentTimeMillis())
3. **Data Persistence**: Data is volatile (lost on application restart)
4. **Concurrency**: The application is designed for moderate concurrent load
5. **Email Uniqueness**: Enforced at application level, case-sensitive
6. **Business Hours**: No need for time zone handling
7. **Audit Trail**: All operations are auditable
8. **Reference Data**: Fixed reference data (no dynamic updates)
9. **Error Handling**: All exceptions are non-fatal (no circuit breakers)
10. **Privacy**: No sensitive data masking (can be added as enhancement)

## Conclusion

The implementation follows Spring Boot best practices with a clean, layered architecture. The design prioritizes:
- **Clarity**: Easy to understand and maintain
- **Testability**: Each layer can be tested independently
- **Extensibility**: Easy to add new features or switch storage
- **Maintainability**: Clear separation of concerns
- **Compliance**: Meets all requirements in the specification

The design allows for easy transition to a database-backed solution in the future while maintaining the same API contracts and business logic.
