package com.example.deepfake.file.dto;

import com.example.deepfake.common.dto.PaginationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileListResponse {
    
    private Boolean success;
    private FileListData data;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileListData {
        private List<FileItemDto> files;
        private PaginationDto pagination;
    }
}
