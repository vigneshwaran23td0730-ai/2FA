#!/bin/bash

echo "=========================================="
echo "Starting Secure 2FA Authentication System"
echo "=========================================="
echo ""

# Make mvnw executable
chmod +x mvnw

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java not found. Installing OpenJDK 17..."
    sudo apt-get update
    sudo apt-get install -y openjdk-17-jdk
    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH
fi

echo "Java version:"
java -version
echo ""

echo "Starting application with embedded H2 database..."
echo "This will run without Docker, MySQL, or Redis"
echo ""

# Run the application with embedded profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=embedded

echo ""
echo "Application stopped."
