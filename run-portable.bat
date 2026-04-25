@echo off
echo ========================================
echo  Secure 2FA Authentication System
echo  Portable Runner (No Installation)
echo ========================================
echo.

REM Check if portable Java exists
if exist "portable-java\bin\java.exe" (
    echo ✓ Using portable Java...
    set JAVA_HOME=%CD%\portable-java
    set PATH=%JAVA_HOME%\bin;%PATH%
) else (
    echo ❌ Portable Java not found!
    echo.
    echo Please download portable Java 17:
    echo 1. Go to: https://adoptium.net/temurin/releases/
    echo 2. Download "JDK 17 LTS" for Windows x64 (ZIP version)
    echo 3. Extract to "portable-java" folder in this directory
    echo 4. Run this script again
    echo.
    pause
    exit /b 1
)

REM Check if Maven wrapper exists
if not exist "mvnw.cmd" (
    echo ❌ Maven wrapper not found!
    pause
    exit /b 1
)

echo ✓ Starting application with embedded database...
echo.
echo 📱 Application will be available at:
echo    🌐 Main API: http://localhost:8080
echo    📚 Swagger UI: http://localhost:8080/swagger-ui.html
echo    💚 Health: http://localhost:8080/actuator/health
echo.
echo 📧 Note: Email features will be simulated (no real emails sent)
echo.

REM Set environment for embedded mode
set SPRING_PROFILES_ACTIVE=embedded
set SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
set SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
set SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.H2Dialect
set APP_SMS_ENABLED=false

REM Start the application
echo 🚀 Starting Spring Boot application...
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=embedded

pause