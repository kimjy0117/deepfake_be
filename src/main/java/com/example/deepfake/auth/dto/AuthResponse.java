package com.example.deepfake.auth.dto;

import com.example.deepfake.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private Boolean success;
    private String message;
    private AuthData data;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthData {
        private UserDto user;
        private TokenResponse tokens;
    }
}
