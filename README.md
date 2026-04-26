# Secure 2FA Authentication System

A comprehensive, production-ready Spring Boot 3.x application providing secure two-factor authentication (2FA) with OTP and TOTP support.

## Features

### Core Authentication
- **User Registration**: Secure user account creation with validation
- **Password Authentication**: BCrypt-hashed password storage and verification
- **JWT Token Management**: Secure access token generation and validation
- **Session Management**: Stateless authentication with JWT

### Two-Factor Authentication
- **OTP (One-Time Password)**: Time-based codes sent via email/SMS
- **TOTP (Time-based OTP)**: Google Authenticator compatible
- **Multiple Delivery Methods**: Email, SMS, or both
- **QR Code Generation**: Easy authenticator app setup



### Security Features
- **Rate Limiting**: Prevents brute force attacks-8
- **Attempt Tracking**: Monitors and logs authentication attempts
- **Redis-backed Storage**: Secure OTP storage with TTL
- **IP-based Restrictions**: Additional security layer
- **Password Policy**: Strong password requirements

### Infrastructure
- **MySQL/PostgreSQL**: Persistent data storage
- **Redis**: Caching and session management
- **Docker Support**: Complete containerization
- **Health Monitoring**: Actuator endpoints
- **API Documentation**: Swagger/OpenAPI integration

## Technology Stack

- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Security 6**
- **Spring Data JPA**
- **Spring Data Redis**
- **MySQL 8.0**
- **Redis 7**
- **JWT (JJWT)**
- **Twilio** (SMS provider)
- **Docker & Docker Compose**
- **Maven**

## Quick Start

### 🚀 Fastest Way: GitHub Codespaces (Recommended)

**No installation needed! Run in your browser:**

1. Open this repository in GitHub Codespaces
2. Run the quick start script:
```bash
chmod +x run-codespaces.sh
./run-codespaces.sh
```

3. Access the API:
   - **Health Check**: http://localhost:8080/api/health
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **API Docs**: http://localhost:8080/v3/api-docs

4. For frontend integration:
   - Go to **PORTS** tab in Codespaces
   - Make port **8080** public
   - Copy the forwarded URL and use it in your frontend

📖 **See [CODESPACES_QUICKSTART.md](CODESPACES_QUICKSTART.md) for detailed instructions**

---

### 🐳 Docker Compose (Full Production Setup)

**Prerequisites**: Docker and Docker Compose

```bash
# Clone and setup
git clone https://github.com/vigneshwaran23td0730-ai/2FA.git
cd 2FA
cp .env.example .env

# Start all services (app, MySQL, Redis, MailHog)
docker-compose up -d

# View logs
docker-compose logs -f app
```

**Access Points:**
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **MailHog UI**: http://localhost:8025 (for testing emails)

---

### 💻 Local Development (Without Docker)

**Prerequisites**: Java 17+, Maven 3.6+

```bash
# Clone repository
git clone https://github.com/vigneshwaran23td0730-ai/2FA.git
cd 2FA

# Run with embedded H2 database (no MySQL/Redis needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=embedded
```

**Access Points:**
- **API**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## Development Setup

### Local Development
```bash
# Start dependencies only
docker-compose up -d mysql redis mailhog

# Run application locally
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Running Tests
```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# With coverage
./mvnw clean test jacoco:report
```

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "password": "SecurePass123!"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

#### Request OTP
```http
POST /api/auth/request-otp
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "deliveryMethod": "EMAIL"
}
```

#### Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "otp": "123456"
}
```

#### Enable TOTP
```http
POST /api/auth/enable-totp
Authorization: Bearer <jwt-token>
```

#### Verify TOTP
```http
POST /api/auth/verify-totp
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "totpCode": "123456"
}
```

#### Get User Profile
```http
GET /api/auth/me
Authorization: Bearer <jwt-token>
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <jwt-token>
```

## Configuration

### Environment Variables

#### Database Configuration
```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=secure_auth
DB_USERNAME=root
DB_PASSWORD=password
```

#### Redis Configuration
```env
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

#### JWT Configuration
```env
JWT_SECRET=your-very-secure-jwt-secret-key-here
JWT_EXPIRATION=3600
```

#### Email Configuration
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

#### SMS Configuration (Twilio)
```env
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=+1234567890
SMS_ENABLED=true
```

### Application Profiles

- **dev**: Development profile with debug logging
- **prod**: Production profile with optimized settings
- **test**: Test profile with H2 database

## Security Considerations

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

### Rate Limiting
- OTP requests: 10 per hour per user/IP
- OTP verification: 5 attempts per OTP
- Failed login tracking and temporary lockout

### Data Protection
- Passwords hashed with BCrypt (strength 12)
- OTPs stored as SHA-256 hashes in Redis
- JWT tokens signed with secure keys
- No sensitive data in logs

## Monitoring and Health

### Health Endpoints
```http
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

### Logging
- Structured logging with correlation IDs
- Security event logging
- Performance metrics
- Error tracking and alerting

## Deployment

### Docker Production Deployment
```bash
# Build production image
docker build -t secure-2fa-auth:latest .

# Run with production profile
docker run -d \
  --name secure-2fa-auth \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e REDIS_HOST=your-redis-host \
  secure-2fa-auth:latest
```

### Kubernetes Deployment
```yaml
# Example deployment configuration
apiVersion: apps/v1
kind: Deployment
metadata:
  name: secure-2fa-auth
spec:
  replicas: 3
  selector:
    matchLabels:
      app: secure-2fa-auth
  template:
    metadata:
      labels:
        app: secure-2fa-auth
    spec:
      containers:
      - name: app
        image: secure-2fa-auth:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        # Add other environment variables
```

## Testing

### Sample cURL Commands

#### Register a new user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "password": "SecurePass123!"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123!"
  }'
```

#### Request OTP
```bash
curl -X POST http://localhost:8080/api/auth/request-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "deliveryMethod": "EMAIL"
  }'
```

### Postman Collection
Import the following collection structure:

```json
{
  "info": {
    "name": "Secure 2FA Auth API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Authentication",
      "item": [
        {
          "name": "Register User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"John Doe\",\n  \"email\": \"john.doe@example.com\",\n  \"phone\": \"+1234567890\",\n  \"password\": \"SecurePass123!\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/auth/register",
              "host": ["{{baseUrl}}"],
              "path": ["api", "auth", "register"]
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    }
  ]
}
```

## Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check MySQL container
docker-compose logs mysql

# Verify connection
docker-compose exec mysql mysql -u root -p -e "SHOW DATABASES;"
```

#### Redis Connection Issues
```bash
# Check Redis container
docker-compose logs redis

# Test Redis connection
docker-compose exec redis redis-cli ping
```

#### Email Delivery Issues
- Check MailHog UI at http://localhost:8025
- Verify SMTP configuration in application.yml
- Check application logs for email errors

#### SMS Delivery Issues
- Verify Twilio credentials
- Check SMS_ENABLED configuration
- Review Twilio console for delivery status

### Performance Tuning

#### JVM Options
```bash
# Production JVM settings
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

#### Database Optimization
```sql
-- Add indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_otp_attempts_user_created ON otp_attempts(user_id, created_at);
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Check the documentation
- Review the troubleshooting section

## Changelog

### Version 1.0.0
- Initial release
- Complete 2FA authentication system
- OTP and TOTP support
- Docker containerization
- Comprehensive test suite