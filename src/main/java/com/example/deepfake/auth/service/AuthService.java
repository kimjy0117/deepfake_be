package com.example.deepfake.auth.service;

import com.example.deepfake.auth.dto.LoginRequest;
import com.example.deepfake.auth.dto.RefreshTokenRequest;
import com.example.deepfake.auth.dto.RegisterRequest;
import com.example.deepfake.auth.dto.AuthResponse;
import com.example.deepfake.auth.dto.TokenResponse;

public interface AuthService {
    
    AuthResponse register(RegisterRequest request);
    
    AuthResponse login(LoginRequest request);
    
    TokenResponse refreshToken(RefreshTokenRequest request);
}
