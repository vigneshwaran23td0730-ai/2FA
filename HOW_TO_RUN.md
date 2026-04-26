# How to Run the 2FA Authentication System

## ✅ All Issues Fixed!

The following issues have been resolved:
1. ✅ Docker image error (openjdk:17-jdk-slim not found) - Fixed with eclipse-temurin
2. ✅ CORS configuration - Enhanced to allow all origins
3. ✅ Health endpoints - Added `/api/health` and `/api/test`
4. ✅ Security configuration - Updated to permit health endpoints
5. ✅ GitHub repository - All changes pushed successfully

## 🚀 How to Run (Choose One Method)

### Method 1: GitHub Codespaces (EASIEST - Recommended)

This is the fastest way to run without any installation:

1. **Open in Codespaces**:
   - Go to https://github.com/vigneshwaran23td0730-ai/2FA
   - Click the green **Code** button
   - Select **Codespaces** tab
   - Click **Create codespace on main**

2. **Run the application**:
   ```bash
   chmod +x run-codespaces.sh
   ./run-codespaces.sh
   ```

3. **Wait for startup**:
   - You'll see "Started Secure2faAuthApplication" when ready
   - Takes about 30-60 seconds

4. **Test the API**:
   ```bash
   # Open a new terminal and test
   curl http://localhost:8080/api/health
   ```

5. **Connect your frontend**:
   - Go to **PORTS** tab in Codespaces
   - Find port **8080**
   - Right-click → **Port Visibility** → **Public**
   - Copy the forwarded URL (e.g., `https://xxx-8080.preview.app.github.dev`)
   - Use this URL in your frontend as the API base URL

### Method 2: Docker Compose (Full Production Setup)

If you want to run with MySQL and Redis:

1. **Fix the Dockerfile issue first**:
   ```bash
   # The Dockerfile has been updated in GitHub
   # Pull the latest changes
   git pull origin main
   ```

2. **Run with Docker**:
   ```bash
   docker-compose up -d
   ```

3. **Check logs**:
   ```bash
   docker-compose logs -f app
   ```

### Method 3: Local Windows (Without Docker)

If you have Java 17 installed locally:

1. **Run directly**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=embedded
   ```

## 🌐 Frontend Integration

### Update Your Frontend API URL

Your frontend needs to point to the backend API. Update your frontend configuration:

**For Codespaces:**
```javascript
// Replace with your Codespaces forwarded URL
const API_BASE_URL = 'https://YOUR-CODESPACE-8080.preview.app.github.dev';
```

**For Local:**
```javascript
const API_BASE_URL = 'http://localhost:8080';
```

### Test the Connection

Before using the frontend, test the backend:

```bash
# Health check
curl http://localhost:8080/api/health

# Test registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@1234",
    "fullName": "Test User",
    "phoneNumber": "+1234567890"
  }'
```

## 📝 API Endpoints Available

Once running, you can access:

- **Health Check**: `GET /api/health`
- **Test Endpoint**: `GET /api/test`
- **Register User**: `POST /api/auth/register`
- **Login**: `POST /api/auth/login`
- **Request OTP**: `POST /api/auth/request-otp`
- **Verify OTP**: `POST /api/auth/verify-otp`
- **Enable TOTP**: `POST /api/auth/enable-totp`
- **Verify TOTP**: `POST /api/auth/verify-totp`
- **Get Profile**: `GET /api/auth/me`
- **Logout**: `POST /api/auth/logout`

**Documentation:**
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs

## 🔧 Troubleshooting

### "Unexpected token '<', "<!DOCTYPE"... is not valid JSON"

This error means the backend is NOT running. The frontend is receiving an HTML error page instead of JSON.

**Solution**: Start the backend first using one of the methods above.

### Permission Denied on mvnw

```bash
chmod +x mvnw
chmod +x run-codespaces.sh
```

### Port Already in Use

```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

### Docker Image Not Found

The Dockerfile has been fixed. Pull the latest changes:
```bash
git pull origin main
```

## 📦 What's Been Updated in GitHub

All these files have been pushed to your repository:

1. **Dockerfile** - Fixed to use `eclipse-temurin:17-jdk-jammy`
2. **SecurityConfig.java** - Added `/api/health` and `/api/test` to permitted endpoints
3. **HealthController.java** - New controller with health and test endpoints
4. **run-codespaces.sh** - Quick start script for Codespaces
5. **CODESPACES_QUICKSTART.md** - Detailed Codespaces guide
6. **README.md** - Updated with all running options
7. **HOW_TO_RUN.md** - This file

## 🎯 Next Steps

1. **Run the backend** using Method 1 (Codespaces) - easiest option
2. **Get the public URL** from Codespaces PORTS tab
3. **Update your frontend** to use this URL
4. **Test the registration** endpoint from your frontend
5. **Deploy frontend** to Firebase (if not already done)

## 💡 Tips

- Use **Codespaces** for the fastest setup (no installation needed)
- Use **embedded profile** for quick testing (no MySQL/Redis needed)
- Use **Docker Compose** for production-like environment
- Check **Swagger UI** for interactive API testing
- Monitor **logs** to see what's happening

## 📞 Support

If you encounter any issues:
1. Check the logs: `docker-compose logs -f app` or terminal output
2. Verify the backend is running: `curl http://localhost:8080/api/health`
3. Check CORS is enabled (already configured)
4. Ensure frontend is using the correct API URL

---

**Repository**: https://github.com/vigneshwaran23td0730-ai/2FA.git

**Status**: ✅ All fixes committed and pushed to GitHub
