package com.example.deepfake.gallery.service;

import com.example.deepfake.gallery.dto.GalleryStatsResponse;
import com.example.deepfake.file.repository.FileRepository;
import com.example.deepfake.gallery.service.GalleryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GalleryServiceImpl implements GalleryService {
    
    private final FileRepository fileRepository;
    
    @Override
    public GalleryStatsResponse getGalleryStats() {
        Long totalFiles = fileRepository.countTotalFiles();
        Long totalImages = fileRepository.countImages();
        Long totalVideos = fileRepository.countVideos();
        Long totalUsers = fileRepository.countUsers();
        Long totalSize = fileRepository.sumTotalSize();
        
        GalleryStatsResponse.GalleryStatsData statsData = GalleryStatsResponse.GalleryStatsData.builder()
                .totalFiles(totalFiles.intValue())
                .totalImages(totalImages.intValue())
                .totalVideos(totalVideos.intValue())
                .totalUsers(totalUsers.intValue())
                .totalSize(totalSize)
                .build();
        
        return GalleryStatsResponse.builder()
                .success(true)
                .data(statsData)
                .build();
    }
}
