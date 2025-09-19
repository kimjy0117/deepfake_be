package com.example.deepfake.file.controller;

import com.example.deepfake.file.dto.FileUpdateRequest;
import com.example.deepfake.file.dto.FileDetailResponse;
import com.example.deepfake.file.dto.FileItemDto;
import com.example.deepfake.file.dto.FileListResponse;
import com.example.deepfake.file.dto.PublicFileListResponse;
import com.example.deepfake.common.dto.SuccessResponse;
import com.example.deepfake.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.deepfake.auth.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Files", description = "파일 관리 관련 API")
public class FileController {
    
    private final FileService fileService;
    private final JwtService jwtService;
    
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.getUserIdFromToken(token);
        }
        throw new RuntimeException("인증 토큰이 없습니다");
    }
    
    @PostMapping("/upload")
    @Operation(summary = "파일 업로드", description = "이미지 또는 영상 파일을 업로드합니다")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SuccessResponse<List<FileItemDto>>> uploadFiles(
            @RequestParam(name = "files") List<MultipartFile> files,
            @RequestParam(name = "titles") List<String> titles,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("파일 업로드 요청: 사용자 {}, 파일 개수 {}", userId, files.size());
        List<FileItemDto> response = fileService.uploadFiles(files, titles, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.<List<FileItemDto>>builder()
                        .success(true)
                        .message("파일 업로드가 완료되었습니다")
                        .data(response)
                        .build());
    }
    
    @GetMapping("/my")
    @Operation(summary = "내 파일 목록 조회", description = "현재 사용자가 업로드한 파일 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FileListResponse> getMyFiles(
            @Parameter(description = "파일 타입 필터") @RequestParam(name = "type", defaultValue = "all") String type,
            @Parameter(description = "페이지 번호") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(name = "sort", defaultValue = "uploadedAt") String sort,
            @Parameter(description = "정렬 순서") @RequestParam(name = "order", defaultValue = "desc") String order,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("내 파일 목록 조회 요청: 사용자 {}, 타입 {}", userId, type);
        
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sort));
        
        FileListResponse response = fileService.getMyFiles(userId, type, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/public")
    @Operation(summary = "공개 파일 목록 조회", description = "모든 사용자가 업로드한 공개 파일 목록을 조회합니다")
    public ResponseEntity<PublicFileListResponse> getPublicFiles(
            @Parameter(description = "파일 타입 필터") @RequestParam(name = "type", defaultValue = "all") String type,
            @Parameter(description = "페이지 번호") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(name = "sort", defaultValue = "uploadedAt") String sort,
            @Parameter(description = "정렬 순서") @RequestParam(name = "order", defaultValue = "desc") String order) {
        log.info("공개 파일 목록 조회 요청: 타입 {}", type);
        
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sort));
        
        PublicFileListResponse response = fileService.getPublicFiles(type, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "파일 검색", description = "제목, 파일명, 업로더명으로 파일을 검색합니다")
    public ResponseEntity<PublicFileListResponse> searchFiles(
            @Parameter(description = "검색 키워드") @RequestParam(name = "q") String q,
            @Parameter(description = "파일 타입 필터") @RequestParam(name = "type", defaultValue = "all") String type,
            @Parameter(description = "페이지 번호") @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "20") int size) {
        log.info("파일 검색 요청: 키워드 {}, 타입 {}", q, type);
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        
        PublicFileListResponse response = fileService.searchFiles(q, type, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{fileId}")
    @Operation(summary = "파일 상세 정보 조회", description = "특정 파일의 상세 정보를 조회합니다")
    public ResponseEntity<FileDetailResponse> getFileDetail(
            @Parameter(description = "파일 ID") @PathVariable("fileId") Long fileId) {
        log.info("파일 상세 정보 조회 요청: {}", fileId);
        FileDetailResponse response = fileService.getFileDetail(fileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/static/{fileName:.+}")
    @Operation(summary = "파일 직접 서빙", description = "업로드된 파일을 직접 서빙합니다 (이미지 등)")
    public ResponseEntity<byte[]> serveFileByName(@PathVariable("fileName") String fileName) {
        try {
            Path filePath = Paths.get("./uploads", fileName);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileData = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileData);
        } catch (IOException e) {
            log.error("파일 서빙 중 오류 발생: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{fileId}")
    @Operation(summary = "파일 삭제", description = "본인이 업로드한 파일을 삭제합니다")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SuccessResponse<Void>> deleteFile(
            @Parameter(description = "파일 ID") @PathVariable("fileId") Long fileId,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("파일 삭제 요청: 파일 {}, 사용자 {}", fileId, userId);
        fileService.deleteFile(fileId, userId);
        return ResponseEntity.ok(SuccessResponse.<Void>builder()
                .success(true)
                .message("파일이 삭제되었습니다")
                .build());
    }
    
    @PutMapping("/{fileId}")
    @Operation(summary = "파일 정보 수정", description = "파일의 제목 등 메타데이터를 수정합니다")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SuccessResponse<FileItemDto>> updateFile(
            @Parameter(description = "파일 ID") @PathVariable("fileId") Long fileId,
            @Valid @RequestBody FileUpdateRequest updateRequest,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("파일 정보 수정 요청: 파일 {}, 사용자 {}", fileId, userId);
        FileItemDto response = fileService.updateFile(fileId, updateRequest, userId);
        return ResponseEntity.ok(SuccessResponse.<FileItemDto>builder()
                .success(true)
                .message("파일 정보가 수정되었습니다")
                .data(response)
                .build());
    }
    
    @GetMapping("/{fileId}/download")
    @Operation(summary = "파일 다운로드", description = "파일을 다운로드합니다")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "파일 ID") @PathVariable("fileId") Long fileId) {
        log.info("파일 다운로드 요청: {}", fileId);
        byte[] fileData = fileService.downloadFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileData);
    }
    
    @GetMapping("/{fileId}/stream")
    @Operation(summary = "파일 스트리밍 (Redirect to Cloudinary)", description = "Cloudinary URL로 리디렉션합니다")
    public ResponseEntity<Void> streamFile(
            @Parameter(description = "파일 ID") @PathVariable("fileId") Long fileId) {
        log.info("파일 스트리밍 요청 (Cloudinary 리디렉션): {}", fileId);
        
        try {
            // 파일 상세 정보 조회
            FileDetailResponse fileDetail = fileService.getFileDetail(fileId);
            String cloudinaryUrl = fileDetail.getData().getUrl();
            
            log.info("Cloudinary URL로 리디렉션: {} -> {}", fileId, cloudinaryUrl);
            
            // Cloudinary URL로 리디렉션
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(URI.create(cloudinaryUrl))
                    .build();
                    
        } catch (Exception e) {
            log.error("파일 리디렉션 중 오류 발생: {}", fileId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/serve/{fileName}")
    @Operation(summary = "파일 서빙 (Deprecated)", description = "이 기능은 더 이상 지원되지 않습니다. Cloudinary URL을 직접 사용하세요.")
    @Deprecated
    public ResponseEntity<String> serveFile(@PathVariable("fileName") String fileName) {
        log.warn("serveFile() 메서드는 deprecated되었습니다. Cloudinary URL을 직접 사용하세요: {}", fileName);
        return ResponseEntity.status(HttpStatus.GONE)
                .body("이 기능은 더 이상 지원되지 않습니다. 파일 상세 API를 통해 Cloudinary URL을 조회하여 직접 접근하세요.");
    }
}
