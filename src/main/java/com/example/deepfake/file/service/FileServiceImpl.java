package com.example.deepfake.file.service;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import com.example.deepfake.file.dto.FileUpdateRequest;
import com.example.deepfake.file.dto.FileDetailResponse;
import com.example.deepfake.file.dto.FileItemDto;
import com.example.deepfake.file.dto.FileListResponse;
import com.example.deepfake.file.dto.PublicFileListResponse;
import com.example.deepfake.dto.response.PublicFileItemDto;
import com.example.deepfake.file.entity.File;
import com.example.deepfake.file.repository.FileRepository;
import com.example.deepfake.user.entity.User;
import com.example.deepfake.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileServiceImpl implements FileService {
    
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    
    @Value("${cloudinary.folder:deepfake}")
    private String cloudinaryFolder;
    
    @Override
    public List<FileItemDto> uploadFiles(List<MultipartFile> files, List<String> titles, Long userId) {
        log.info("파일 업로드 시작 (Cloudinary): 사용자 {}, 파일 개수 {}", userId, files.size());
        
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        List<FileItemDto> uploadedFiles = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String title = (titles != null && i < titles.size()) ? titles.get(i) : file.getOriginalFilename();
            
            try {
                FileItemDto uploadedFile = uploadSingleFileToCloudinary(file, title, user);
                uploadedFiles.add(uploadedFile);
                log.info("Cloudinary 업로드 성공: {}", uploadedFile.getName());
            } catch (Exception e) {
                log.error("Cloudinary 업로드 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + file.getOriginalFilename(), e);
            }
        }
        
        log.info("Cloudinary 파일 업로드 완료: {} 개 파일", uploadedFiles.size());
        return uploadedFiles;
    }
    
    private FileItemDto uploadSingleFileToCloudinary(MultipartFile file, String title, User user) throws IOException {
        // 파일 검증
        if (file.isEmpty()) {
            throw new RuntimeException("빈 파일입니다");
        }
        
        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString();
        
        // 파일 타입 결정
        File.FileType fileType = determineFileType(file.getContentType());
        
        try {
            // Cloudinary 업로드 옵션 설정
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", cloudinaryFolder);
            uploadParams.put("public_id", uniqueFileName);
            uploadParams.put("resource_type", fileType == File.FileType.VIDEO ? "video" : "image");
            
            // 이미지인 경우 최적화 옵션 추가
            if (fileType == File.FileType.IMAGE) {
                uploadParams.put("transformation", "c_limit,w_1920,h_1080,q_auto");
            }
            
            // Cloudinary에 업로드
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            
            log.info("Cloudinary 업로드 완료: {} -> {}", originalFilename, cloudinaryUrl);
            
            // 썸네일 URL 생성 (이미지만)
            String thumbnailUrl = null;
            if (fileType == File.FileType.IMAGE) {
                String baseUrl = cloudinaryUrl.substring(0, cloudinaryUrl.lastIndexOf('/') + 1);
                thumbnailUrl = baseUrl + "c_fill,w_300,h_200,q_auto/" + publicId;
            }
            
            // 파일 엔티티 생성 및 저장
            File fileEntity = File.builder()
                .name(publicId)
                .title(title)
                .originalName(originalFilename)
                .url(cloudinaryUrl)
                .thumbnailUrl(thumbnailUrl)
                .size(file.getSize())
                .type(fileType)
                .mimeType(file.getContentType())
                .user(user)
                .build();
            
            File savedFile = fileRepository.save(fileEntity);
            
            log.info("파일 엔티티 저장 완료: ID {}", savedFile.getId());
            
            // DTO 변환
            return FileItemDto.builder()
                .id(savedFile.getId())
                .name(savedFile.getName())
                .title(savedFile.getTitle())
                .originalName(savedFile.getOriginalName())
                .url(savedFile.getUrl())
                .thumbnailUrl(savedFile.getThumbnailUrl())
                .size(savedFile.getSize())
                .type(savedFile.getType())
                .mimeType(savedFile.getMimeType())
                .userId(savedFile.getUser().getId())
                .uploadedAt(savedFile.getUploadedAt())
                .build();
                
        } catch (Exception e) {
            log.error("Cloudinary 업로드 중 오류 발생: {}", originalFilename, e);
            throw new RuntimeException("Cloudinary 업로드 실패: " + e.getMessage(), e);
        }
    }
    
    
    private File.FileType determineFileType(String mimeType) {
        if (mimeType == null) {
            return File.FileType.IMAGE; // 기본값
        }
        
        if (mimeType.startsWith("image/")) {
            return File.FileType.IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return File.FileType.VIDEO;
        } else {
            return File.FileType.IMAGE; // 기본값
        }
    }
    
    private FileItemDto convertToFileItemDto(File file) {
        // Cloudinary URL 직접 사용
        return FileItemDto.builder()
            .id(file.getId())
            .name(file.getName())
            .title(file.getTitle())
            .originalName(file.getOriginalName())
            .url(file.getUrl()) // Cloudinary URL 직접 사용
            .thumbnailUrl(file.getThumbnailUrl())
            .size(file.getSize())
            .type(file.getType())
            .mimeType(file.getMimeType())
            .userId(file.getUser().getId())
            .uploadedAt(file.getUploadedAt())
            .build();
    }
    
    private PublicFileItemDto convertToPublicFileItemDto(File file) {
        // Cloudinary URL 직접 사용
        return PublicFileItemDto.publicBuilder()
            .id(file.getId())
            .name(file.getName())
            .title(file.getTitle())
            .originalName(file.getOriginalName())
            .url(file.getUrl()) // Cloudinary URL 직접 사용
            .thumbnailUrl(file.getThumbnailUrl())
            .size(file.getSize())
            .type(file.getType())
            .mimeType(file.getMimeType())
            .userId(file.getUser().getId())
            .uploadedAt(file.getUploadedAt())
            .userName(file.getUser().getName())
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public FileListResponse getMyFiles(Long userId, String type, Pageable pageable) {
        log.info("사용자 파일 목록 조회: 사용자 {}, 타입 {}", userId, type);
        
        // 파일 타입에 따른 조회
        org.springframework.data.domain.Page<File> filePage;
        if ("all".equalsIgnoreCase(type)) {
            filePage = fileRepository.findByUserIdOrderByUploadedAtDesc(userId, pageable);
        } else {
            File.FileType fileType = File.FileType.valueOf(type.toUpperCase());
            filePage = fileRepository.findByUserIdAndTypeOrderByUploadedAtDesc(userId, fileType, pageable);
        }
        
        // DTO 변환
        List<FileItemDto> files = filePage.getContent().stream()
            .map(this::convertToFileItemDto)
            .toList();
        
        // 페이징 정보 생성
        com.example.deepfake.common.dto.PaginationDto pagination = com.example.deepfake.common.dto.PaginationDto.builder()
            .currentPage(pageable.getPageNumber() + 1)
            .pageSize(pageable.getPageSize())
            .totalPages(filePage.getTotalPages())
            .totalElements(filePage.getTotalElements())
            .hasNext(filePage.hasNext())
            .hasPrevious(filePage.hasPrevious())
            .build();
        
        // 데이터 객체 생성
        FileListResponse.FileListData data = FileListResponse.FileListData.builder()
            .files(files)
            .pagination(pagination)
            .build();
        
        return FileListResponse.builder()
            .success(true)
            .data(data)
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PublicFileListResponse getPublicFiles(String type, Pageable pageable) {
        log.info("공개 파일 목록 조회: 타입 {}", type);
        
        // 파일 타입에 따른 조회
        org.springframework.data.domain.Page<File> filePage;
        if ("all".equalsIgnoreCase(type)) {
            filePage = fileRepository.findAllByOrderByUploadedAtDesc(pageable);
        } else {
            File.FileType fileType = File.FileType.valueOf(type.toUpperCase());
            filePage = fileRepository.findByTypeOrderByUploadedAtDesc(fileType, pageable);
        }
        
        // DTO 변환 (사용자 이름 포함)
        List<PublicFileItemDto> files = filePage.getContent().stream()
            .map(this::convertToPublicFileItemDto)
            .toList();
        
        // 페이징 정보 생성
        com.example.deepfake.common.dto.PaginationDto pagination = com.example.deepfake.common.dto.PaginationDto.builder()
            .currentPage(pageable.getPageNumber() + 1)
            .pageSize(pageable.getPageSize())
            .totalPages(filePage.getTotalPages())
            .totalElements(filePage.getTotalElements())
            .hasNext(filePage.hasNext())
            .hasPrevious(filePage.hasPrevious())
            .build();
        
        // PublicFileListData 생성
        PublicFileListResponse.PublicFileListData data = PublicFileListResponse.PublicFileListData.builder()
            .files(files)
            .pagination(pagination)
            .build();
        
        return PublicFileListResponse.builder()
            .success(true)
            .data(data)
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PublicFileListResponse searchFiles(String keyword, String type, Pageable pageable) {
        log.info("파일 검색: 키워드 {}, 타입 {}", keyword, type);
        
        // 파일 타입에 따른 검색
        org.springframework.data.domain.Page<File> filePage;
        if ("all".equalsIgnoreCase(type)) {
            filePage = fileRepository.findByTitleContainingOrOriginalNameContainingOrderByUploadedAtDesc(keyword, keyword, pageable);
        } else {
            File.FileType fileType = File.FileType.valueOf(type.toUpperCase());
            filePage = fileRepository.findByTypeAndTitleContainingOrTypeAndOriginalNameContainingOrderByUploadedAtDesc(
                fileType, keyword, fileType, keyword, pageable);
        }
        
        // DTO 변환 (사용자 이름 포함)
        List<PublicFileItemDto> files = filePage.getContent().stream()
            .map(this::convertToPublicFileItemDto)
            .toList();
        
        // 페이징 정보 생성
        com.example.deepfake.common.dto.PaginationDto pagination = com.example.deepfake.common.dto.PaginationDto.builder()
            .currentPage(pageable.getPageNumber() + 1)
            .pageSize(pageable.getPageSize())
            .totalPages(filePage.getTotalPages())
            .totalElements(filePage.getTotalElements())
            .hasNext(filePage.hasNext())
            .hasPrevious(filePage.hasPrevious())
            .build();
        
        // PublicFileListData 생성
        PublicFileListResponse.PublicFileListData data = PublicFileListResponse.PublicFileListData.builder()
            .files(files)
            .pagination(pagination)
            .build();
        
        return PublicFileListResponse.builder()
            .success(true)
            .data(data)
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public FileDetailResponse getFileDetail(Long fileId) {
        log.info("파일 상세 정보 조회: {}", fileId);
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        // PublicFileItemDto 생성 (Cloudinary URL 직접 사용)
        PublicFileItemDto fileData = PublicFileItemDto.publicBuilder()
            .id(file.getId())
            .name(file.getName())
            .title(file.getTitle())
            .originalName(file.getOriginalName())
            .url(file.getUrl()) // Cloudinary URL 직접 사용
            .thumbnailUrl(file.getThumbnailUrl())
            .size(file.getSize())
            .type(file.getType())
            .mimeType(file.getMimeType())
            .userId(file.getUser().getId())
            .userName(file.getUser().getName())
            .uploadedAt(file.getUploadedAt())
            .build();
        
        return FileDetailResponse.builder()
            .success(true)
            .data(fileData)
            .build();
    }
    
    @Override
    public void deleteFile(Long fileId, Long userId) {
        log.info("파일 삭제: 파일 {}, 사용자 {}", fileId, userId);
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        // 권한 확인
        if (!file.getUser().getId().equals(userId)) {
            throw new RuntimeException("파일 삭제 권한이 없습니다");
        }
        
        // Cloudinary에서 파일 삭제
        try {
            String publicId = file.getName(); // public_id로 저장되어 있음
            String resourceType = file.getType() == File.FileType.VIDEO ? "video" : "image";
            
            Map deleteResult = cloudinary.uploader().destroy(publicId, Map.of("resource_type", resourceType));
            log.info("Cloudinary에서 파일 삭제 완료: {}, 결과: {}", publicId, deleteResult.get("result"));
        } catch (Exception e) {
            log.error("Cloudinary에서 파일 삭제 실패: {}", file.getName(), e);
            // 파일 삭제 실패해도 DB에서는 제거
        }
        
        // DB에서 삭제
        fileRepository.delete(file);
        log.info("파일 메타데이터 삭제 완료: {}", fileId);
    }
    
    @Override
    public FileItemDto updateFile(Long fileId, FileUpdateRequest request, Long userId) {
        log.info("파일 정보 수정: 파일 {}, 사용자 {}", fileId, userId);
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        // 권한 확인
        if (!file.getUser().getId().equals(userId)) {
            throw new RuntimeException("파일 수정 권한이 없습니다");
        }
        
        // 파일 정보 수정
        if (request.getTitle() != null) {
            file.setTitle(request.getTitle());
        }
        
        File updatedFile = fileRepository.save(file);
        log.info("파일 정보 수정 완료: {}", fileId);
        
        return convertToFileItemDto(updatedFile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadFile(Long fileId) {
        // Cloudinary 사용시 직접 URL 제공 - 이 메서드는 더이상 사용되지 않습니다.
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        log.warn("downloadFile() 메서드는 Cloudinary 사용으로 deprecated됩니다. 직접 URL을 사용하세요: {}", file.getUrl());
        throw new RuntimeException("Cloudinary 사용으로 인해 파일은 직접 URL을 통해 접근 가능합니다. URL: " + file.getUrl());
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] streamFile(Long fileId, String range) {
        // Cloudinary 사용시 직접 URL 제공 - 이 메서드는 더이상 사용되지 않습니다.
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        log.warn("streamFile() 메서드는 Cloudinary 사용으로 deprecated됩니다. 직접 URL을 사용하세요: {}", file.getUrl());
        throw new RuntimeException("Cloudinary 사용으로 인해 파일은 직접 URL을 통해 접근 가능합니다. URL: " + file.getUrl());
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getFileMimeType(Long fileId) {
        log.info("파일 MIME 타입 조회: {}", fileId);
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        return file.getMimeType();
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getFileSize(Long fileId) {
        log.info("파일 크기 조회: {}", fileId);
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        return file.getSize();
    }
}
