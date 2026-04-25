# 🚀 Quick Start Guide - No Docker Required

## Option 1: 📱 Online IDE (Fastest - 2 minutes)

### Using GitHub Codespaces
1. **Go to:** https://github.com
2. **Create new repository** (public or private)
3. **Upload all project files** from your `C:\Users\Vigneshwaran\Desktop\2FA` folder
4. **Click:** Code → Codespaces → Create codespace
5. **In terminal run:** `docker-compose up -d`
6. **Access:** Click the "Ports" tab and open port 8080

### Using Replit
1. **Go to:** https://replit.com
2. **Create new Repl** → Import from GitHub
3. **Upload your project files**
4. **Click Run**

## Option 2: 💻 Portable Java Setup (5 minutes)

### Step 1: Download Portable Java
1. **Go to:** https://adoptium.net/temurin/releases/
2. **Download:** OpenJDK 17 LTS for Windows x64 (ZIP version)
3. **Extract** the ZIP file to a folder named `portable-java` in your project directory

### Step 2: Run the Application
```cmd
# Double-click this file:
run-portable.bat
```

**Or manually:**
```cmd
# Set Java path
set JAVA_HOME=%CD%\portable-java
set PATH=%JAVA_HOME%\bin;%PATH%

# Run application
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=embedded
```

## Option 3: 🌐 Cloud Development Environments

### Gitpod (Free)
1. **Go to:** https://gitpod.io
2. **Sign up** with GitHub/GitLab
3. **Create workspace** from your repository
4. **Run:** `docker-compose up -d`

### CodeSandbox
1. **Go to:** https://codesandbox.io
2. **Import** your project
3. **Run** in browser

### Stackblitz
1. **Go to:** https://stackblitz.com
2. **Create** new project
3. **Upload** files

## Option 4: 📱 Mobile/Tablet Development

### Using Termux (Android)
```bash
# Install Java and Git
pkg install openjdk-17 git

# Clone your project
git clone <your-repo-url>
cd secure-2fa-auth

# Run application
./mvnw spring-boot:run -Dspring-boot.run.profiles=embedded
```

## 🎯 What Each Option Gives You

### Embedded Mode Features:
- ✅ **In-memory H2 Database** (no MySQL needed)
- ✅ **Simple caching** (no Redis needed)
- ✅ **Mock email service** (logs emails to console)
- ✅ **All authentication features** working
- ✅ **Full API access** via Swagger UI
- ✅ **TOTP/2FA functionality** complete

### Access Points (All Options):
- **🌐 Main API:** http://localhost:8080
- **📚 Swagger UI:** http://localhost:8080/swagger-ui.html
- **💚 Health Check:** http://localhost:8080/actuator/health
- **🗄️ Database Console:** http://localhost:8080/h2-console

## 🚀 Recommended: GitHub Codespaces

**Fastest and easiest option:**

1. **Create GitHub account** (free)
2. **Create repository** and upload your project
3. **Open in Codespaces** (free tier available)
4. **Run:** `docker-compose up -d`
5. **Access your app** in seconds!

## 📱 Test Your Application

Once running, test with these cURL commands:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"SecurePass123!"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecurePass123!"}'
```

## 🎉 You're Ready!

Choose any option above and you'll have your **complete 2FA authentication system** running in minutes without installing anything locally!

**Recommended order:**
1. 🥇 **GitHub Codespaces** (easiest)
2. 🥈 **Portable Java** (local control)
3. 🥉 **Gitpod** (alternative cloud)

All options give you the **full functionality** of your secure 2FA system! 🚀