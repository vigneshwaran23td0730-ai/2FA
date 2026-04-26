/**
 * Mock API Service to allow the application to run purely on the frontend (Firebase Hosting)
 * while simulating backend behavior using localStorage.
 */

class MockApiService {
    constructor() {
        this.DB_KEY = 'mock_users_db';
        this.initDb();
    }

    initDb() {
        if (!localStorage.getItem(this.DB_KEY)) {
            localStorage.setItem(this.DB_KEY, JSON.stringify([]));
        }
    }

    getUsers() {
        return JSON.parse(localStorage.getItem(this.DB_KEY));
    }

    saveUser(user) {
        const users = this.getUsers();
        const index = users.findIndex(u => u.email === user.email);
        if (index >= 0) {
            users[index] = user;
        } else {
            users.push(user);
        }
        localStorage.setItem(this.DB_KEY, JSON.stringify(users));
    }

    getToken() {
        return localStorage.getItem('jwt_token');
    }

    setToken(token) {
        localStorage.setItem('jwt_token', token);
    }

    removeToken() {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('current_user_email');
    }

    getCurrentEmail() {
        return localStorage.getItem('current_user_email');
    }

    setCurrentEmail(email) {
        localStorage.setItem('current_user_email', email);
    }

    async register(name, email, password) {
        console.log('Mock Register:', { name, email });
        const users = this.getUsers();
        if (users.find(u => u.email === email)) {
            throw new Error('User already exists');
        }

        const newUser = {
            name,
            email,
            password, // In a real app, this would be hashed
            totpEnabled: false,
            totpSecret: null,
            otp: null,
            otpExpiry: null
        };

        this.saveUser(newUser);
        return { status: 'success', message: 'User registered successfully' };
    }

    async login(email, password) {
        console.log('Mock Login:', email);
        const users = this.getUsers();
        const user = users.find(u => u.email === email && u.password === password);

        if (!user) {
            throw new Error('Invalid credentials');
        }

        this.setCurrentEmail(email);

        // Always require 2FA for this demo to show the flow
        return {
            status: 'success',
            data: {
                requires2fa: true,
                totpEnabled: user.totpEnabled
            }
        };
    }

    async requestOtp(email) {
        console.log('Mock OTP Request:', email);
        const users = this.getUsers();
        const user = users.find(u => u.email === email);
        if (!user) throw new Error('User not found');

        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        user.otp = otp;
        this.saveUser(user);

        // In a real app, this would be emailed. For the demo, we show it in an alert/console
        console.log('*********************************');
        console.log(`MOCK OTP FOR ${email}: ${otp}`);
        console.log('*********************************');
        
        // Let the user know the code for the demo
        setTimeout(() => {
            alert(`[DEMO MODE] Your 2FA code is: ${otp}`);
        }, 500);

        return { status: 'success', message: 'OTP sent' };
    }

    async verifyOtp(email, otpCode) {
        console.log('Mock Verify OTP:', { email, otpCode });
        const users = this.getUsers();
        const user = users.find(u => u.email === email);

        if (!user || user.otp !== otpCode) {
            throw new Error('Invalid or expired OTP');
        }

        user.otp = null;
        this.saveUser(user);
        
        const token = 'mock-jwt-token-' + Math.random().toString(36).substr(2);
        return {
            status: 'success',
            data: { token }
        };
    }

    async getProfile() {
        const email = this.getCurrentEmail();
        const users = this.getUsers();
        const user = users.find(u => u.email === email);

        if (!user) throw new Error('Unauthorized');

        return {
            status: 'success',
            data: {
                name: user.name,
                email: user.email,
                totpEnabled: user.totpEnabled
            }
        };
    }

    async enableTotp() {
        const email = this.getCurrentEmail();
        const secret = 'MOCKSECRET' + Math.random().toString(36).toUpperCase().substr(2, 6);
        
        // Mock QR code image (using a placeholder)
        const qrCode = 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=otpauth://totp/Secure2FA:' + email + '?secret=' + secret + '&issuer=Secure2FA';

        return {
            status: 'success',
            data: {
                secret: secret,
                qrCodeImageBase64: qrCode
            }
        };
    }

    async verifyTotp(totpCode) {
        // In mock mode, any 6 digit code works for setup
        if (!/^\d{6}$/.test(totpCode)) {
            throw new Error('Invalid TOTP code format');
        }

        const email = this.getCurrentEmail();
        const users = this.getUsers();
        const user = users.find(u => u.email === email);

        user.totpEnabled = true;
        this.saveUser(user);

        return { status: 'success', message: 'TOTP enabled' };
    }

    async logout() {
        this.removeToken();
        return { status: 'success' };
    }
}

const api = new MockApiService();
