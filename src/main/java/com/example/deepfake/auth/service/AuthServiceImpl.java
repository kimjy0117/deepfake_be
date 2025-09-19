package com.example.deepfake.auth.service;

import com.example.deepfake.auth.dto.LoginRequest;
import com.example.deepfake.auth.dto.RefreshTokenRequest;
import com.example.deepfake.auth.dto.RegisterRequest;
import com.example.deepfake.auth.dto.AuthResponse;
import com.example.deepfake.auth.dto.TokenResponse;
import com.example.deepfake.user.dto.UserDto;
import com.example.deepfake.user.entity.User;
import com.example.deepfake.user.repository.UserRepository;
import com.example.deepfake.auth.service.AuthService;
import com.example.deepfake.auth.service.JwtService;
import com.example.deepfake.common.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("이미 존재하는 이메일입니다");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 사용자 생성 및 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .build();
        
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        
        TokenResponse tokens = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600)
                .build();
        
        UserDto userDto = UserDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
        
        // 응답 반환
        return AuthResponse.builder()
                .success(true)
                .message("회원가입이 완료되었습니다")
                .data(AuthResponse.AuthData.builder()
                        .user(userDto)
                        .tokens(tokens)
                        .build())
                .build();
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다"));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
        
        // JWT 토큰 생성
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        TokenResponse tokens = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600)
                .build();
        
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        
        // 응답 반환
        return AuthResponse.builder()
                .success(true)
                .message("로그인이 완료되었습니다")
                .data(AuthResponse.AuthData.builder()
                        .user(userDto)
                        .tokens(tokens)
                        .build())
                .build();
    }
    
    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        // 리프레시 토큰 검증
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
        }
        
        // 토큰에서 사용자 정보 추출
        String email = jwtService.getEmailFromToken(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        // 새로운 액세스 토큰 생성
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        // 응답 반환
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(3600)
                .build();
    }
}
