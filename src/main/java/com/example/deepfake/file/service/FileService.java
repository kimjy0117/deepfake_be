package com.example.deepfake.file.service;

import com.example.deepfake.file.dto.FileUpdateRequest;
import com.example.deepfake.file.dto.FileDetailResponse;
import com.example.deepfake.file.dto.FileItemDto;
import com.example.deepfake.file.dto.FileListResponse;
import com.example.deepfake.file.dto.PublicFileListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    
    List<FileItemDto> uploadFiles(List<MultipartFile> files, List<String> titles, Long userId);
    
    FileListResponse getMyFiles(Long userId, String type, Pageable pageable);
    
    PublicFileListResponse getPublicFiles(String type, Pageable pageable);
    
    PublicFileListResponse searchFiles(String keyword, String type, Pageable pageable);
    
    FileDetailResponse getFileDetail(Long fileId);
    
    void deleteFile(Long fileId, Long userId);
    
    FileItemDto updateFile(Long fileId, FileUpdateRequest request, Long userId);
    
    byte[] downloadFile(Long fileId);
    
    byte[] streamFile(Long fileId, String range);
    
    String getFileMimeType(Long fileId);
    
    long getFileSize(Long fileId);
}
