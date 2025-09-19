package com.example.deepfake.user.controller;

import com.example.deepfake.user.dto.UserDto;
import com.example.deepfake.user.service.UserService;
import com.example.deepfake.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.deepfake.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "사용자 정보 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.getUserIdFromToken(token);
        }
        throw new RuntimeException("인증 토큰이 없습니다");
    }
    
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    public ResponseEntity<SuccessResponse<UserDto>> getCurrentUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("사용자 정보 조회 요청: {}", userId);
        UserDto userData = userService.getCurrentUser(userId);
        
        SuccessResponse<UserDto> response = SuccessResponse.<UserDto>builder()
                .success(true)
                .message("사용자 정보 조회 성공")
                .data(userData)
                .build();
                
        return ResponseEntity.ok(response);
    }
}
