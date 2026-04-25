# Project Structure

```
secure-2fa-auth/
├── src/
│   ├── main/
│   │   ├── java/com/auth/
│   │   │   ├── Secure2faAuthApplication.java          # Main Spring Boot application
│   │   │   ├── config/                                # Configuration classes
│   │   │   │   ├── OpenApiConfig.java                 # Swagger/OpenAPI configuration
│   │   │   │   ├── RedisConfig.java                   # Redis configuration
│   │   │   │   └── SecurityConfig.java                # Spring Security configuration
│   │   │   ├── controller/                            # REST controllers
│   │   │   │   └── AuthController.java                # Authentication endpoints
│   │   │   ├── dto/                                   # Data Transfer Objects
│   │   │   │   ├── ApiResponse.java                   # Generic API response wrapper
│   │   │   │   ├── AuthResponse.java                  # Authentication response
│   │   │   │   ├── LoginRequest.java                  # Login request DTO
│   │   │   │   ├── OtpRequest.java                    # OTP request DTO
│   │   │   │   ├── OtpVerificationRequest.java        # OTP verification DTO
│   │   │   │   ├── RegisterRequest.java               # User registration DTO
│   │   │   │   ├── TotpSetupRequest.java              # TOTP setup DTO
│   │   │   │   ├── TotpSetupResponse.java             # TOTP setup response
│   │   │   │   └── UserProfileResponse.java           # User profile response
│   │   │   ├── entity/                                # JPA entities
│   │   │   │   ├── OtpAttempt.java                    # OTP attempt audit entity
│   │   │   │   └── User.java                          # User entity
│   │   │   ├── exception/                             # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java       # Global exception handler
│   │   │   │   ├── InvalidCredentialsException.java  # Invalid credentials exception
│   │   │   │   ├── InvalidTotpException.java          # Invalid TOTP exception
│   │   │   │   ├── RateLimitExceededException.java    # Rate limit exception
│   │   │   │   ├── UserAlreadyExistsException.java    # User exists exception
│   │   │   │   └── UserNotFoundException.java         # User not found exception
│   │   │   ├── repository/                            # Data access layer
│   │   │   │   ├── OtpAttemptRepository.java          # OTP attempt repository
│   │   │   │   └── UserRepository.java                # User repository
│   │   │   ├── security/                              # Security components
│   │   │   │   ├── JwtAuthenticationEntryPoint.java   # JWT authentication entry point
│   │   │   │   ├── JwtAuthenticationFilter.java       # JWT authentication filter
│   │   │   │   └── UserPrincipal.java                 # User principal for security context
│   │   │   ├── service/                               # Business logic layer
│   │   │   │   ├── AuthService.java                   # Main authentication service
│   │   │   │   ├── JwtService.java                    # JWT token service
│   │   │   │   ├── NotificationService.java           # Email/SMS notification service
│   │   │   │   ├── OtpService.java                    # OTP generation and verification
│   │   │   │   ├── SmsService.java                    # SMS service (Twilio)
│   │   │   │   ├── TotpService.java                   # TOTP service
│   │   │   │   └── UserDetailsServiceImpl.java        # Spring Security user details service
│   │   │   └── util/                                  # Utility classes
│   │   │       ├── IpAddressUtil.java                 # IP address extraction utility
│   │   │       ├── OtpGenerator.java                  # OTP generation utility
│   │   │       └── QrCodeGenerator.java               # QR code generation utility
│   │   └── resources/
│   │       ├── application.yml                        # Main application configuration
│   │       ├── application-dev.yml                    # Development profile configuration
│   │       ├── application-docker.yml                 # Docker profile configuration
│   │       └── application-prod.yml                   # Production profile configuration
│   └── test/
│       ├── java/com/auth/
│       │   ├── Secure2faAuthApplicationTests.java     # Main application test
│       │   ├── controller/
│       │   │   └── AuthControllerIntegrationTest.java # Integration tests for auth controller
│       │   └── service/
│       │       ├── AuthServiceTest.java               # Unit tests for auth service
│       │       └── OtpServiceTest.java                # Unit tests for OTP service
│       └── resources/
│           └── application-test.yml                   # Test profile configuration
├── .mvn/wrapper/                                      # Maven wrapper
│   └── maven-wrapper.properties                      # Maven wrapper properties
├── docker-compose.yml                                # Docker Compose configuration
├── docker-compose.override.yml                       # Docker Compose development overrides
├── Dockerfile                                         # Docker image configuration
├── .env.example                                       # Environment variables example
├── .gitignore                                         # Git ignore rules
├── mvnw                                              # Maven wrapper script (Unix)
├── mvnw.cmd                                          # Maven wrapper script (Windows)
├── pom.xml                                           # Maven project configuration
├── PROJECT_STRUCTURE.md                             # This file
├── README.md                                         # Project documentation
└── test-api.sh                                       # API testing script
```

## Architecture Overview

### Layered Architecture
The application follows a clean layered architecture:

1. **Controller Layer** (`controller/`)
   - REST API endpoints
   - Request/response handling
   - Input validation
   - HTTP status management

2. **Service Layer** (`service/`)
   - Business logic implementation
   - Transaction management
   - Integration between components
   - Security enforcement

3. **Repository Layer** (`repository/`)
   - Data access abstraction
   - JPA repository interfaces
   - Custom query methods
   - Database operations

4. **Entity Layer** (`entity/`)
   - JPA entities
   - Database table mappings
   - Relationships and constraints
   - Audit fields

### Security Architecture

1. **Authentication Flow**
   ```
   User Request → JWT Filter → Security Context → Controller → Service
   ```

2. **2FA Flow**
   ```
   Login → Password Check → 2FA Required? → OTP/TOTP → JWT Token
   ```

3. **Rate Limiting**
   ```
   Request → Rate Limit Check → Redis Counter → Allow/Deny
   ```

### Data Flow

1. **User Registration**
   ```
   Controller → Validation → Service → Password Hash → Repository → Database
   ```

2. **OTP Generation**
   ```
   Service → OTP Generate → Hash → Redis Store → Notification Service → Email/SMS
   ```

3. **JWT Authentication**
   ```
   Login → JWT Generate → Redis Cache → Response → Subsequent Requests → JWT Verify
   ```

## Key Design Patterns

### 1. Repository Pattern
- Abstracts data access logic
- Provides clean separation between business logic and data layer
- Enables easy testing with mocks

### 2. Service Layer Pattern
- Encapsulates business logic
- Provides transaction boundaries
- Enables code reuse across controllers

### 3. DTO Pattern
- Separates internal models from API contracts
- Provides validation at API boundary
- Enables API versioning

### 4. Builder Pattern
- Used in entity creation
- Provides fluent API for object construction
- Ensures immutability where needed

### 5. Strategy Pattern
- Used for notification delivery methods (Email/SMS/Both)
- Enables easy addition of new delivery channels
- Provides runtime selection of strategies

## Security Features

### 1. Password Security
- BCrypt hashing with strength 12
- Strong password policy enforcement
- No plaintext password storage

### 2. OTP Security
- SHA-256 hashed storage in Redis
- Time-based expiration (5 minutes)
- Attempt limiting (5 attempts max)
- Rate limiting (10 requests per hour)

### 3. JWT Security
- Secure signing with configurable secret
- Short expiration times (1 hour default)
- Stateless authentication
- Proper claims validation

### 4. Rate Limiting
- User-based and IP-based limits
- Redis-backed counters
- Configurable time windows
- Audit trail for security monitoring

### 5. Input Validation
- Bean validation annotations
- Custom validation rules
- SQL injection prevention
- XSS protection

## Configuration Management

### 1. Profile-based Configuration
- **dev**: Development with debug logging
- **prod**: Production with optimized settings
- **test**: Testing with H2 database
- **docker**: Container-specific settings

### 2. Environment Variables
- Sensitive data externalized
- Docker-friendly configuration
- Easy deployment across environments

### 3. Feature Flags
- SMS enabling/disabling
- Debug logging control
- Environment-specific features

## Testing Strategy

### 1. Unit Tests
- Service layer testing with mocks
- Utility class testing
- Security component testing

### 2. Integration Tests
- Full application context testing
- Database integration testing
- Security integration testing

### 3. API Testing
- REST endpoint testing
- Authentication flow testing
- Error handling testing

## Deployment Architecture

### 1. Containerization
- Multi-stage Docker builds
- Non-root user execution
- Health check implementation
- Resource optimization

### 2. Service Dependencies
- MySQL for persistent data
- Redis for caching and sessions
- MailHog for email testing
- Twilio for SMS delivery

### 3. Monitoring
- Actuator health endpoints
- Application metrics
- Structured logging
- Error tracking

This architecture provides a solid foundation for a production-ready 2FA authentication system with proper separation of concerns, security best practices, and maintainable code structure.