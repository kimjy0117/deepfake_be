package com.example.deepfake.common.config;

import com.example.deepfake.user.entity.User;
import com.example.deepfake.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        createTestUsers();
    }
    
    private void createTestUsers() {
        // 테스트 사용자 1
        if (!userRepository.existsByEmail("user@example.com")) {
            User user1 = User.builder()
                    .email("user@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("홍길동")
                    .build();
            
            userRepository.save(user1);
            log.info("테스트 사용자 생성됨: user@example.com");
        }
        
        // 테스트 사용자 2
        if (!userRepository.existsByEmail("admin@example.com")) {
            User user2 = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .name("관리자")
                    .build();
            
            userRepository.save(user2);
            log.info("테스트 사용자 생성됨: admin@example.com");
        }
        
        log.info("데이터 초기화 완료");
    }
}
