#!/bin/bash

echo "🚀 Starting Secure 2FA Authentication System in Codespaces"
echo "=========================================================="
echo

# Check if Java is available
if command -v java &> /dev/null; then
    echo "✅ Java found: $(java -version 2>&1 | head -n 1)"
else
    echo "❌ Java not found, installing..."
    sudo apt-get update
    sudo apt-get install -y openjdk-17-jdk
fi

# Check if Maven is available
if command -v mvn &> /dev/null; then
    echo "✅ Maven found: $(mvn -version | head -n 1)"
else
    echo "❌ Maven not found, using Maven wrapper..."
fi

echo
echo "🔧 Starting application with embedded database..."
echo "📱 This will run without Docker using H2 in-memory database"
echo

# Set environment for embedded mode
export SPRING_PROFILES_ACTIVE=embedded
export SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
export SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.H2Dialect
export APP_SMS_ENABLED=false

# Make mvnw executable
chmod +x mvnw

echo "🚀 Starting Spring Boot application..."
echo "📍 Application will be available at:"
echo "   🌐 Main API: https://$CODESPACE_NAME-8080.app.github.dev"
echo "   📚 Swagger UI: https://$CODESPACE_NAME-8080.app.github.dev/swagger-ui.html"
echo "   💚 Health: https://$CODESPACE_NAME-8080.app.github.dev/actuator/health"
echo "   🗄️ H2 Console: https://$CODESPACE_NAME-8080.app.github.dev/h2-console"
echo

# Start the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=embedded