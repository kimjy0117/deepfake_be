package com.example.deepfake.file.dto;

import com.example.deepfake.dto.response.PublicFileItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDetailResponse {
    
    private Boolean success;
    private PublicFileItemDto data;
}
