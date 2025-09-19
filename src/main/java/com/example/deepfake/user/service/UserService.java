package com.example.deepfake.user.service;

import com.example.deepfake.user.dto.UserDto;

public interface UserService {
    
    UserDto getCurrentUser(Long userId);
}
