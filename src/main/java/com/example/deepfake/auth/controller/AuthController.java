package com.example.deepfake.auth.controller;

import com.example.deepfake.auth.dto.LoginRequest;
import com.example.deepfake.auth.dto.RefreshTokenRequest;
import com.example.deepfake.auth.dto.RegisterRequest;
import com.example.deepfake.auth.dto.AuthResponse;
import com.example.deepfake.auth.dto.TokenResponse;
import com.example.deepfake.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "사용자 인증 관련 API")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("회원가입 요청: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("토큰 갱신 요청");
        TokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
