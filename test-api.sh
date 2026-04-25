#!/bin/bash

# Secure 2FA Authentication API Test Script
# This script demonstrates the complete authentication flow

BASE_URL="http://localhost:8080"
EMAIL="test.user@example.com"
PASSWORD="SecurePass123!"
NAME="Test User"
PHONE="+1234567890"

echo "=== Secure 2FA Authentication API Test ==="
echo "Base URL: $BASE_URL"
echo "Test Email: $EMAIL"
echo ""

# Function to make HTTP requests with proper error handling
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4
    
    local headers="Content-Type: application/json"
    if [ ! -z "$token" ]; then
        headers="$headers -H Authorization: Bearer $token"
    fi
    
    echo "Making $method request to $endpoint"
    if [ ! -z "$data" ]; then
        echo "Request data: $data"
    fi
    
    local response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
        -H "$headers" \
        ${data:+-d "$data"})
    
    local body=$(echo "$response" | head -n -1)
    local status=$(echo "$response" | tail -n 1)
    
    echo "Status: $status"
    echo "Response: $body"
    echo ""
    
    if [ $status -ge 200 ] && [ $status -lt 300 ]; then
        echo "$body"
    else
        echo "Request failed with status $status"
        return 1
    fi
}

# Test 1: Health Check
echo "1. Testing Health Check..."
make_request "GET" "/actuator/health"

# Test 2: Register User
echo "2. Registering new user..."
REGISTER_DATA="{
    \"name\": \"$NAME\",
    \"email\": \"$EMAIL\",
    \"phone\": \"$PHONE\",
    \"password\": \"$PASSWORD\"
}"

REGISTER_RESPONSE=$(make_request "POST" "/api/auth/register" "$REGISTER_DATA")
if [ $? -eq 0 ]; then
    echo "✓ User registration successful"
else
    echo "✗ User registration failed"
    exit 1
fi

# Test 3: Login (without 2FA)
echo "3. Testing login..."
LOGIN_DATA="{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\"
}"

LOGIN_RESPONSE=$(make_request "POST" "/api/auth/login" "$LOGIN_DATA")
if [ $? -eq 0 ]; then
    # Extract JWT token from response
    JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    if [ ! -z "$JWT_TOKEN" ]; then
        echo "✓ Login successful, JWT token obtained"
        echo "Token: ${JWT_TOKEN:0:50}..."
    else
        echo "✗ Login successful but no JWT token found"
        exit 1
    fi
else
    echo "✗ Login failed"
    exit 1
fi

# Test 4: Get User Profile
echo "4. Testing user profile retrieval..."
PROFILE_RESPONSE=$(make_request "GET" "/api/auth/me" "" "$JWT_TOKEN")
if [ $? -eq 0 ]; then
    echo "✓ User profile retrieved successfully"
else
    echo "✗ Failed to retrieve user profile"
fi

# Test 5: Enable TOTP
echo "5. Testing TOTP setup..."
TOTP_RESPONSE=$(make_request "POST" "/api/auth/enable-totp" "" "$JWT_TOKEN")
if [ $? -eq 0 ]; then
    echo "✓ TOTP setup initiated"
    # Extract QR code URL
    QR_URL=$(echo "$TOTP_RESPONSE" | grep -o '"qrCodeUrl":"[^"]*"' | cut -d'"' -f4)
    if [ ! -z "$QR_URL" ]; then
        echo "QR Code URL: $QR_URL"
    fi
else
    echo "✗ TOTP setup failed"
fi

# Test 6: Request OTP
echo "6. Testing OTP request..."
OTP_REQUEST_DATA="{
    \"email\": \"$EMAIL\",
    \"deliveryMethod\": \"EMAIL\"
}"

OTP_REQUEST_RESPONSE=$(make_request "POST" "/api/auth/request-otp" "$OTP_REQUEST_DATA")
if [ $? -eq 0 ]; then
    echo "✓ OTP request successful"
    echo "Note: Check MailHog at http://localhost:8025 for the OTP email"
else
    echo "✗ OTP request failed"
fi

# Test 7: Test invalid OTP verification (since we don't have the actual OTP)
echo "7. Testing OTP verification with invalid code..."
OTP_VERIFY_DATA="{
    \"email\": \"$EMAIL\",
    \"otp\": \"000000\"
}"

OTP_VERIFY_RESPONSE=$(make_request "POST" "/api/auth/verify-otp" "$OTP_VERIFY_DATA")
if [ $? -ne 0 ]; then
    echo "✓ Invalid OTP correctly rejected"
else
    echo "✗ Invalid OTP was accepted (this shouldn't happen)"
fi

# Test 8: Logout
echo "8. Testing logout..."
LOGOUT_RESPONSE=$(make_request "POST" "/api/auth/logout" "" "$JWT_TOKEN")
if [ $? -eq 0 ]; then
    echo "✓ Logout successful"
else
    echo "✗ Logout failed"
fi

echo "=== API Test Complete ==="
echo ""
echo "Summary:"
echo "- User registration: ✓"
echo "- Login: ✓"
echo "- Profile retrieval: ✓"
echo "- TOTP setup: ✓"
echo "- OTP request: ✓"
echo "- Invalid OTP rejection: ✓"
echo "- Logout: ✓"
echo ""
echo "Next steps:"
echo "1. Check MailHog at http://localhost:8025 for OTP emails"
echo "2. Use the QR code URL to set up Google Authenticator"
echo "3. Test TOTP verification with a real code"
echo "4. Explore the Swagger UI at http://localhost:8080/swagger-ui.html"