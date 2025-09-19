package com.example.deepfake.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private Boolean success;
    private String errorCode;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
}
