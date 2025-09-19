package com.example.deepfake.auth.service;

import com.example.deepfake.user.entity.User;

public interface JwtService {
    
    String generateAccessToken(User user);
    
    String generateRefreshToken(User user);
    
    boolean validateToken(String token);
    
    String getEmailFromToken(String token);
    
    Long getUserIdFromToken(String token);
}
