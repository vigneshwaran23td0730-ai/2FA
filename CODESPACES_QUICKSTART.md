# GitHub Codespaces Quick Start Guide

## 🚀 Fastest Way to Run (No Docker Required)

### Step 1: Make the script executable and run
```bash
chmod +x run-codespaces.sh
./run-codespaces.sh
```

That's it! The application will:
- Install Java 17 if needed
- Use embedded H2 database (no MySQL needed)
- Use in-memory cache (no Redis needed)
- Start on port 8080

### Step 2: Test the API

Once you see "Started Secure2faAuthApplication", open a new terminal and test:

```bash
# Test health endpoint
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

### Step 3: Access from Frontend

If your frontend is on Firebase, you need to expose the port:

1. Go to the **PORTS** tab in Codespaces
2. Find port **8080**
3. Right-click and select **Port Visibility** → **Public**
4. Copy the forwarded URL (e.g., `https://xxx-8080.preview.app.github.dev`)
5. Update your frontend to use this URL as the API base URL

### Alternative: Manual Run

If you prefer to run manually:

```bash
# Make mvnw executable
chmod +x mvnw

# Run with embedded profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=embedded
```

## 📝 API Endpoints

Once running, access:
- **API Health**: http://localhost:8080/api/health
- **API Test**: http://localhost:8080/api/test
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## 🔧 Troubleshooting

### Permission Denied on mvnw
```bash
chmod +x mvnw
```

### Java Not Found
The script will auto-install Java 17. If it fails:
```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk
```

### Port Already in Use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

## 🌐 Frontend Integration

Update your frontend API base URL to:
- **Local testing**: `http://localhost:8080`
- **Codespaces public**: `https://YOUR-CODESPACE-8080.preview.app.github.dev`

Make sure CORS is enabled (already configured in the backend).

## 📦 Production Deployment

For production with Docker:
```bash
docker-compose up -d
```

This will use MySQL and Redis for production-grade setup.
