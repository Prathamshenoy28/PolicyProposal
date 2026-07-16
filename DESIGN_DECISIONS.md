# Design Decisions Document

## Project Architecture

### Layered Architecture (Controller → Service → Repository)

The application implements a clean, layered architecture with clear separation of concerns:

```
┌─────────────────────────┐
│   Controller Layer      │  REST Endpoints & Input Validation
├─────────────────────────┤
│   Service Layer         │  Business Logic & Rules
├─────────────────────────┤
│   Repository Layer      │  Data Access & Storage
├─────────────────────────┤
│   In-Memory Storage     │  Java Collections (ConcurrentHashMap)
└─────────────────────────┘
```

**Rationale:**
- Provides clear separation of concerns
- Makes the code maintainable and testable
- Allows independent testing of each layer
- Follows Spring Boot best practices
- Easy to add database layer later if needed

## Implementation Decisions

### 1. In-Memory Storage

**Decision:** Use Java Collections (`ConcurrentHashMap`) for storage

**Alternatives Considered:**
- Database (H2, MySQL) - Not permitted per requirements
- Simple HashMap - Not thread-safe
- CopyOnWriteArrayList - Inefficient for this use case

**Why ConcurrentHashMap:**
- Thread-safe without explicit synchronization for most operations
- Provides O(1) average time complexity for get/put operations
- No external dependencies required
- Simple and straightforward implementation
- Good for in-memory storage requirements

### 2. Thread Safety

**Decision:** Use both ConcurrentHashMap and synchronized blocks for critical operations

**Implementation:**
```java
private final Map<String, Customer> customers = new ConcurrentHashMap<>();
private final Object lock = new Object();

public Customer save(Customer customer) {
    synchronized (lock) {
        customers.put(customer.getCustomerId(), customer);
        return customer;
    }
}
```

**Rationale:**
- Ensures atomicity of compound operations (e.g., get-then-check-then-put)
- Prevents race conditions during complex operations
- Critical for audit trail consistency

### 3. ID Generation

**Decision:** Use UUID for all entity IDs

**Alternatives Considered:**
- Sequential IDs - Would require centralized counter (synchronization overhead)
- Timestamp-based IDs - Not guaranteed to be unique with high concurrency
- Custom ID format - More complexity

**Why UUID:**
- Guaranteed uniqueness
- No central counter needed
- Standard approach in distributed systems
- Easy to implement and debug

### 4. Policy Number Generation

**Decision:** Format: `POL{timestamp}{uuid-substring}`

Example: `POL1689000002000ABC12345`

**Rationale:**
- Timestamp provides chronological ordering
- UUID substring ensures uniqueness
- Human-readable prefix ("POL") for identification
- Unlikely to generate duplicates even with concurrent submissions

### 5. Validation Strategy

**Two-level validation approach:**

1. **Input Validation (API Boundary)**
   - Use `@Valid` and `@Constraint` annotations
   - Validate data types, required fields, format
   - Occurs in Controller layer
   - Framework-driven validation using `jakarta.validation`

2. **Business Validation (Service Layer)**
   - Complex business rules (age range, PAN requirements, etc.)
   - Cross-entity validations (nominee vs customer)
   - Reference data validation
   - Occurs in Service layer

**Rationale:**
- Separates concerns clearly
- Input validation is faster (fails fast)
- Business validation can use other services/repositories
- Easier to test and maintain
- Follows Spring best practices

### 6. Exception Handling

**Decision:** Centralized exception handling with custom exceptions

**Custom Exception Hierarchy:**
```
Exception
├── BusinessException (extends RuntimeException)
└── ResourceNotFoundException (extends RuntimeException)
```

**Global Exception Handler:**
- Catches all exceptions at the application level
- Returns consistent error responses
- Maps exceptions to HTTP status codes
- Provides meaningful error messages

**Why RuntimeException:**
- Unchecked exceptions (no need for try-catch)
- Cleaner code
- Can be caught and handled globally
- Follows Spring Boot conventions

### 7. Audit Trail

**Decision:** Create audit records for all significant operations

**Audit Events:**
- Customer creation and updates
- Proposal creation and submission
- Includes timestamp and details

**Rationale:**
- Provides traceability
- Satisfies compliance requirements
- Allows investigation of state changes
- Enables business analytics

### 8. DTO Pattern

**Decision:** Separate Request and Response DTOs

**Advantages:**
- Input validation on request objects
- Response objects can hide sensitive data
- API contracts are explicit
- Decouples API from entity models
- Easy to add/remove fields in future

**Implementation:**
- `CustomerRequest`: For POST/PUT operations
- `CustomerResponse`: For GET operations
- `ProposalRequest`: For POST operations
- `ProposalResponse`: For GET operations

### 9. Service Layer Design

**Decision:** Services handle business logic, repositories handle data access

**Service Responsibilities:**
- Validate business rules
- Coordinate multiple repositories
- Create audit records
- Transform between DTOs and entities
- Manage transactions (if database added later)

**Rationale:**
- Clean separation of concerns
- Business logic is testable independently
- Easy to add cross-cutting concerns
- Facilitates future caching or transaction management

### 10. Reference Data Management

**Decision:** Hard-coded reference data in ReferenceDataRepository

**Alternatives Considered:**
- Load from properties file - More flexibility
- Load from database - Not permitted
- Hard-code in constants - Less extensible

**Why Hard-coded in Repository:**
- Simple and straightforward for this assignment
- Easy to extend
- All reference data in one place
- Can be easily moved to properties file later

**Reference Data:**
```java
POLICY_TERM: [10, 15, 20, 25, 30]
PAYMENT_FREQUENCY: [MONTHLY, QUARTERLY, HALF_YEARLY, ANNUAL]
```

## Testing Strategy

### Unit Testing

**Framework:** JUnit 5

**Test Classes:**
1. **CustomerServiceTest**
   - Customer creation success scenarios
   - Age validation (minimum, maximum)
   - Duplicate email handling
   - Customer update validation
   - Resource not found scenarios
   - Get all customers

2. **ProposalServiceTest**
   - Proposal creation success
   - Policy term validation
   - Sum assured validation (min/max)
   - Annual premium validation
   - PAN validation for high premiums
   - Payment frequency validation
   - Nominee validation
   - Proposal submission flow
   - Cannot submit twice

**Test Approach:**
- Arrange-Act-Assert (AAA) pattern
- Uses in-memory repositories
- No external dependencies
- Fast execution
- Covers happy paths and error cases

### Integration Testing

Not included in this implementation as per requirements, but can be added for:
- Controller-to-Service integration
- End-to-end API testing
- Error response validation

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
