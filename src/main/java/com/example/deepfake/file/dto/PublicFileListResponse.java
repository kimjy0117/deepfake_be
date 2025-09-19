package com.example.deepfake.file.dto;

import com.example.deepfake.common.dto.PaginationDto;
import com.example.deepfake.dto.response.PublicFileItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFileListResponse {
    
    private Boolean success;
    private PublicFileListData data;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublicFileListData {
        private List<PublicFileItemDto> files;
        private PaginationDto pagination;
    }
}
