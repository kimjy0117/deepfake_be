package com.example.deepfake.gallery.controller;

import com.example.deepfake.gallery.dto.GalleryStatsResponse;
import com.example.deepfake.gallery.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gallery")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gallery", description = "갤러리 관련 API")
public class GalleryController {
    
    private final GalleryService galleryService;
    
    @GetMapping("/stats")
    @Operation(summary = "갤러리 통계 조회", description = "전체 갤러리의 통계 정보를 조회합니다")
    public ResponseEntity<GalleryStatsResponse> getGalleryStats() {
        log.info("갤러리 통계 조회 요청");
        GalleryStatsResponse response = galleryService.getGalleryStats();
        return ResponseEntity.ok(response);
    }
}
