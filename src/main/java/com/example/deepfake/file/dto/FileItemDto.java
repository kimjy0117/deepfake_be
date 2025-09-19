package com.example.deepfake.file.dto;

import com.example.deepfake.file.entity.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileItemDto {
    
    private Long id;
    private String name;
    private String title;
    private String originalName;
    private String url;
    private String thumbnailUrl;
    private Long size;
    private com.example.deepfake.file.entity.File.FileType type;
    private String mimeType;
    private Long userId;
    private LocalDateTime uploadedAt;
}
