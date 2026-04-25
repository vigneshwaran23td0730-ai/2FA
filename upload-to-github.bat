@echo off
echo ========================================
echo  Upload to GitHub Repository
echo  Target: https://github.com/vigneshwaran23td0730-ai/2FA.git
echo ========================================
echo.

REM Check if Git is installed
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Git is not installed!
    echo.
    echo Please choose one of these options:
    echo.
    echo 1. 🌐 WEB UPLOAD (Easiest - No installation needed):
    echo    - Go to: https://github.com/vigneshwaran23td0730-ai/2FA
    echo    - Click "Add file" → "Upload files"
    echo    - Drag and drop all files from this folder
    echo    - Add commit message and click "Commit changes"
    echo.
    echo 2. 💻 INSTALL GIT:
    echo    - Download from: https://git-scm.com/download/win
    echo    - Install and restart this script
    echo.
    echo 3. 🖥️ GITHUB DESKTOP:
    echo    - Download from: https://desktop.github.com/
    echo    - Clone repository and copy files
    echo.
    pause
    exit /b 1
)

echo ✓ Git is installed!
echo.

REM Check if this is already a git repository
if exist ".git" (
    echo ✓ Git repository already initialized
) else (
    echo 🔧 Initializing Git repository...
    git init
    if %errorlevel% neq 0 (
        echo ❌ Failed to initialize Git repository
        pause
        exit /b 1
    )
)

REM Add remote repository
echo 🔗 Adding remote repository...
git remote remove origin >nul 2>&1
git remote add origin https://github.com/vigneshwaran23td0730-ai/2FA.git
if %errorlevel% neq 0 (
    echo ❌ Failed to add remote repository
    pause
    exit /b 1
)

REM Add all files
echo 📁 Adding all project files...
git add .
if %errorlevel% neq 0 (
    echo ❌ Failed to add files
    pause
    exit /b 1
)

REM Create commit
echo 💾 Creating commit...
git commit -m "Complete Spring Boot 2FA Authentication System

🔐 Features Implemented:
- User registration and authentication with BCrypt
- OTP generation and verification (Email/SMS delivery)
- TOTP support with Google Authenticator integration
- JWT token management with secure signing
- Rate limiting and brute force protection
- Redis caching for OTP storage and sessions
- MySQL database with proper indexing
- Comprehensive security audit logging
- Docker containerization with multi-stage builds
- Complete API documentation with Swagger/OpenAPI
- Extensive testing suite (Unit + Integration)
- Production-ready deployment configurations

🚀 Technology Stack:
- Java 17 + Spring Boot 3.2.0
- Spring Security 6 + JWT (JJWT)
- MySQL 8.0 + Redis 7
- Docker + Docker Compose
- Maven + JUnit 5 + Mockito
- Twilio SMS + JavaMail
- Comprehensive documentation and guides

📚 Documentation:
- Complete README with quick start guide
- Deployment guide for production environments
- API testing collection (Postman + shell scripts)
- Architecture documentation and best practices
- Docker setup for development and production

✅ Production Ready:
- Security best practices implemented
- Rate limiting and attempt tracking
- Comprehensive error handling
- Health monitoring and metrics
- Scalable stateless architecture
- Complete audit trail for compliance"

if %errorlevel% neq 0 (
    echo ❌ Failed to create commit
    pause
    exit /b 1
)

REM Push to GitHub
echo 🚀 Pushing to GitHub...
git branch -M main
git push -u origin main
if %errorlevel% neq 0 (
    echo ❌ Failed to push to GitHub
    echo.
    echo This might be due to:
    echo - Authentication required (use GitHub CLI or personal access token)
    echo - Repository already has content
    echo - Network connectivity issues
    echo.
    echo Try the web upload method instead:
    echo 1. Go to: https://github.com/vigneshwaran23td0730-ai/2FA
    echo 2. Click "Add file" → "Upload files"
    echo 3. Drag and drop all files from this folder
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ SUCCESS! Project uploaded to GitHub!
echo.
echo 🌐 Repository URL: https://github.com/vigneshwaran23td0730-ai/2FA
echo.
echo 🚀 Next Steps:
echo 1. Visit your repository: https://github.com/vigneshwaran23td0730-ai/2FA
echo 2. Enable GitHub Codespaces for instant development
echo 3. Open in Codespaces and run: docker-compose up -d
echo 4. Access your 2FA app at the provided URL
echo.
echo 📚 Your repository now contains:
echo - Complete Spring Boot 2FA authentication system
echo - Docker containerization setup
echo - Comprehensive documentation and guides
echo - API testing tools and collections
echo - Production deployment configurations
echo.
pause