package com.example.deepfake.gallery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryStatsResponse {
    
    private Boolean success;
    private GalleryStatsData data;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GalleryStatsData {
        private Integer totalFiles;
        private Integer totalImages;
        private Integer totalVideos;
        private Integer totalUsers;
        private Long totalSize;
    }
}
