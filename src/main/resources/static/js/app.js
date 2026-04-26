document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const views = {
        auth: document.getElementById('auth-view'),
        dashboard: document.getElementById('dashboard-view')
    };

    const forms = {
        login: document.getElementById('login-form'),
        register: document.getElementById('register-form'),
        otp: document.getElementById('otp-form'),
        totpLogin: document.getElementById('totp-login-form')
    };

    const elements = {
        loading: document.getElementById('loading'),
        toast: document.getElementById('toast'),
        authTitle: document.getElementById('auth-title'),
        authSubtitle: document.getElementById('auth-subtitle'),
        switchToRegister: document.getElementById('switch-to-register'),
        switchToLogin: document.getElementById('switch-to-login'),
        resendOtp: document.getElementById('resend-otp'),
        
        // Dashboard
        userName: document.getElementById('user-name'),
        userEmail: document.getElementById('user-email'),
        userAvatar: document.getElementById('user-avatar'),
        logoutBtn: document.getElementById('logout-btn'),
        mfaBadge: document.getElementById('mfa-badge'),
        
        // TOTP Setup
        enableTotpBtn: document.getElementById('enable-totp-btn'),
        qrSetupArea: document.getElementById('qr-setup-area'),
        qrCodeImg: document.getElementById('qr-code-img'),
        verifyTotpSetupBtn: document.getElementById('verify-totp-setup-btn'),
        totpSetupCode: document.getElementById('totp-setup-code')
    };

    // State
    let currentEmail = '';
    let loginData = null; // Stores intermediate login data (e.g. requires2fa flag)

    // Helper to show/hide loading
    const setLoading = (isLoading) => {
        if (isLoading) {
            elements.loading.classList.remove('hidden');
        } else {
            elements.loading.classList.add('hidden');
        }
    };

    // Toast Notification
    let toastTimeout;
    const showToast = (message, type = 'success') => {
        elements.toast.textContent = message;
        elements.toast.className = `toast ${type}`;
        elements.toast.classList.remove('hidden');
        
        clearTimeout(toastTimeout);
        toastTimeout = setTimeout(() => {
            elements.toast.classList.add('hidden');
        }, 3000);
    };

    // View Management
    const switchView = (viewName) => {
        Object.values(views).forEach(v => v.classList.add('hidden'));
        views[viewName].classList.remove('hidden');
    };

    const switchAuthForm = (formName) => {
        Object.values(forms).forEach(f => f.classList.add('hidden'));
        forms[formName].classList.remove('hidden');

        if (formName === 'login') {
            elements.authTitle.textContent = 'Welcome Back';
            elements.authSubtitle.textContent = 'Log in to access your secure dashboard';
        } else if (formName === 'register') {
            elements.authTitle.textContent = 'Create Account';
            elements.authSubtitle.textContent = 'Sign up to get started';
        } else if (formName === 'otp' || formName === 'totpLogin') {
            elements.authTitle.textContent = 'Verification Required';
            elements.authSubtitle.textContent = 'Please verify your identity';
        }
    };

    // Check auth status on load
    const checkAuth = async () => {
        if (api.getToken()) {
            await loadDashboard();
        } else {
            switchView('auth');
            switchAuthForm('login');
        }
    };

    // Switch between Login and Register
    elements.switchToRegister.addEventListener('click', (e) => {
        e.preventDefault();
        switchAuthForm('register');
    });

    elements.switchToLogin.addEventListener('click', (e) => {
        e.preventDefault();
        switchAuthForm('login');
    });

    // Handle Registration
    forms.register.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('register-name').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;

        try {
            setLoading(true);
            await api.register(name, email, password);
            showToast('Registration successful! Please log in.');
            switchAuthForm('login');
            document.getElementById('login-email').value = email;
            document.getElementById('login-password').value = password;
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setLoading(false);
        }
    });

    // Handle Login
    forms.login.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        currentEmail = email;

        try {
            setLoading(true);
            const response = await api.login(email, password);
            
            if (response.data.requires2fa) {
                // Determine which 2FA form to show
                if (response.data.totpEnabled) {
                    switchAuthForm('totpLogin');
                } else {
                    // Automatically request OTP
                    await api.requestOtp(email);
                    switchAuthForm('otp');
                }
            } else {
                // Success without 2FA
                api.setToken(response.data.token);
                await loadDashboard();
            }
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setLoading(false);
        }
    });

    // Handle OTP Resend
    elements.resendOtp.addEventListener('click', async (e) => {
        e.preventDefault();
        if (!currentEmail) return;
        
        try {
            setLoading(true);
            await api.requestOtp(currentEmail);
            showToast('Verification code resent');
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setLoading(false);
        }
    });

    // Handle OTP Verification (Email/SMS)
    forms.otp.addEventListener('submit', async (e) => {
        e.preventDefault();
        const code = document.getElementById('otp-code').value;
        
        try {
            setLoading(true);
            const response = await api.verifyOtp(currentEmail, code);
            api.setToken(response.data.token);
            await loadDashboard();
            showToast('Login successful!');
            document.getElementById('otp-code').value = '';
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setLoading(false);
        }
    });
    
    // Handle TOTP Verification (Login)
    forms.totpLogin.addEventListener('submit', async (e) => {
        e.preventDefault();
        const code = document.getElementById('totp-login-code').value;
        
        try {
            setLoading(true);
            // Re-call login API or use verify-otp with the TOTP code depending on backend logic
            // The existing backend verifies TOTP in the verifyOtp endpoint if it sees a 6-digit code
            const response = await api.verifyOtp(currentEmail, code);
            api.setToken(response.data.token);
            await loadDashboard();
            showToast('Login successful!');
            document.getElementById('totp-login-code').value = '';
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setLoading(false);
        }
    });

    // Load Dashboard
    const loadDashboard = async () => {
        try {
            setLoading(true);
            const response = await api.getProfile();
            const user = response.data;
            
            elements.userName.textContent = user.name;
            elements.userEmail.textContent = user.email;
            elements.userAvatar.textContent = user.name.charAt(0).toUpperCase();
            
            updateMfaUI(user.totpEnabled);
            
            switchView('dashboard');
        } catch (error) {
            console.error(error);
            api.removeToken();
            switchView('auth');
            switchAuthForm('login');
        } finally {
            setLoading(false);
        }
    };

    // Update MFA UI State
    const updateMfaUI = (isEnabled) => {
        if (isEnabled) {
            elements.mfaBadge.textContent = 'Enabled';
            elements.mfaBadge.className = 'badge active';
            elements.enableTotpBtn.classList.add('hidden');
            elements.qrSetupArea.classList.add('hidden');
        } else {
            elements.mfaBadge.textContent = 'Disabled';
            elements.mfaBadge.className = 'badge inactive';
            elements.enableTotpBtn.classList.remove('hidden');
        }
    };

    // Handle Enable TOTP
    elements.enableTotpBtn.addEventListener('click', async () => {
        try {
            setLoading(true);
            const response = await api.enableTotp();
            
            // Set QR code image
            elements.qrCodeImg.src = response.data.qrCodeImageBase64;
            
            // Show setup area
            elements.qrSetupArea.classList.remove('hidden');
            elements.enableTotpBtn.classList.add('hidden');
            
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setLoading(false);
        }
    });

    // Handle Verify TOTP Setup
    elements.verifyTotpSetupBtn.addEventListener('click', async () => {
        const code = elements.totpSetupCode.value;
        if (!code) {
            showToast('Please enter the verification code', 'error');
            return;
        }

        try {
            setLoading(true);
            await api.verifyTotp(code);
            showToast('Two-Factor Authentication enabled successfully!');
            updateMfaUI(true);
            elements.totpSetupCode.value = '';
        } catch (error) {
            showToast(error.message, 'error');
        } finally {
            setLoading(false);
        }
    });

    // Handle Logout
    elements.logoutBtn.addEventListener('click', async () => {
        setLoading(true);
        await api.logout();
        setLoading(false);
        
        switchView('auth');
        switchAuthForm('login');
        forms.login.reset();
        showToast('Logged out successfully');
    });

    // Password Visibility Toggle
    document.querySelectorAll('.password-toggle').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.previousElementSibling;
            if (input.type === 'password') {
                input.type = 'text';
                btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="eye-off-icon"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>';
            } else {
                input.type = 'password';
                btn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="eye-icon"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>';
            }
        });
    });

    // Password Strength live validation
    const regPassword = document.getElementById('register-password');
    if (regPassword) {
        regPassword.addEventListener('input', (e) => {
            const val = e.target.value;
            const reqs = {
                'req-length': val.length >= 8,
                'req-lower': /[a-z]/.test(val),
                'req-upper': /[A-Z]/.test(val),
                'req-number': /\d/.test(val),
                'req-special': /[@$!%*?&]/.test(val)
            };
            
            Object.keys(reqs).forEach(id => {
                const el = document.getElementById(id);
                if (el) {
                    if (reqs[id]) {
                        el.classList.add('valid');
                        el.classList.remove('invalid');
                    } else {
                        el.classList.add('invalid');
                        el.classList.remove('valid');
                    }
                }
            });
        });
    }

    // Initialize
    checkAuth();
});
