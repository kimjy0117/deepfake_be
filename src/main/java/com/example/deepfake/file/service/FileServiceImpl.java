package com.example.deepfake.file.service;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileServiceImpl implements FileService {
    
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    @Value("${file.base.url}")
    private String baseUrl;
    
    @Override
    public List<FileItemDto> uploadFiles(List<MultipartFile> files, List<String> titles, Long userId) {
        log.info("파일 업로드 시작: 사용자 {}, 파일 개수 {}", userId, files.size());
        
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        // 업로드 디렉토리 생성
        createUploadDirectory();
        
        List<FileItemDto> uploadedFiles = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String title = (titles != null && i < titles.size()) ? titles.get(i) : file.getOriginalFilename();
            
            try {
                FileItemDto uploadedFile = uploadSingleFile(file, title, user);
                uploadedFiles.add(uploadedFile);
                log.info("파일 업로드 성공: {}", uploadedFile.getName());
            } catch (Exception e) {
                log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + file.getOriginalFilename(), e);
            }
        }
        
        log.info("파일 업로드 완료: {} 개 파일", uploadedFiles.size());
        return uploadedFiles;
    }
    
    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패", e);
        }
    }
    
    private FileItemDto uploadSingleFile(MultipartFile file, String title, User user) throws IOException {
        // 파일 검증
        if (file.isEmpty()) {
            throw new RuntimeException("빈 파일입니다");
        }
        
        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        // 파일 타입 결정
        File.FileType fileType = determineFileType(file.getContentType());
        
        // 파일 저장
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 파일 엔티티 생성 및 저장 (URL은 나중에 설정)
        File fileEntity = File.builder()
            .name(uniqueFileName)
            .title(title)
            .originalName(originalFilename)
            .url("") // 임시로 빈 문자열
            .size(file.getSize())
            .type(fileType)
            .mimeType(file.getContentType())
            .user(user)
            .build();
        
        File savedFile = fileRepository.save(fileEntity);
        
        // 파일 ID 기반 URL 생성 및 업데이트
        String fileUrl = baseUrl + "/files/" + savedFile.getId() + "/stream";
        savedFile.setUrl(fileUrl);
        savedFile = fileRepository.save(savedFile);
        
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
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
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
        // ID 기반 URL 생성
        String fileUrl = baseUrl + "/files/" + file.getId() + "/stream";
        
        return FileItemDto.builder()
            .id(file.getId())
            .name(file.getName())
            .title(file.getTitle())
            .originalName(file.getOriginalName())
            .url(fileUrl)
            .thumbnailUrl(file.getThumbnailUrl())
            .size(file.getSize())
            .type(file.getType())
            .mimeType(file.getMimeType())
            .userId(file.getUser().getId())
            .uploadedAt(file.getUploadedAt())
            .build();
    }
    
    private PublicFileItemDto convertToPublicFileItemDto(File file) {
        // ID 기반 URL 생성
        String fileUrl = baseUrl + "/files/" + file.getId() + "/stream";
        
        return PublicFileItemDto.publicBuilder()
            .id(file.getId())
            .name(file.getName())
            .title(file.getTitle())
            .originalName(file.getOriginalName())
            .url(fileUrl)
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
        
        // ID 기반 URL 생성
        String fileUrl = baseUrl + "/files/" + file.getId() + "/stream";
        
        // PublicFileItemDto 생성
        PublicFileItemDto fileData = PublicFileItemDto.publicBuilder()
            .id(file.getId())
            .name(file.getName())
            .title(file.getTitle())
            .originalName(file.getOriginalName())
            .url(fileUrl)
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
        
        // 실제 파일 삭제
        try {
            Path filePath = Paths.get(uploadDir, file.getName());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 완료: {}", filePath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", file.getName(), e);
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
        log.info("파일 다운로드: {}", fileId);
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        try {
            Path filePath = Paths.get(uploadDir, file.getName());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("파일이 존재하지 않습니다: " + file.getName());
            }
            
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", file.getName(), e);
            throw new RuntimeException("파일 읽기 중 오류가 발생했습니다", e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] streamFile(Long fileId, String range) {
        log.info("파일 스트리밍: {}, Range: {}", fileId, range);
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        
        try {
            Path filePath = Paths.get(uploadDir, file.getName());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("파일이 존재하지 않습니다: " + file.getName());
            }
            
            long fileSize = Files.size(filePath);
            
            // Range 요청이 없으면 전체 파일 반환 (작은 파일의 경우)
            if (range == null || fileSize < 1024 * 1024) { // 1MB 미만
                return Files.readAllBytes(filePath);
            }
            
            // Range 요청 파싱 (예: "bytes=0-1023")
            if (range.startsWith("bytes=")) {
                String[] ranges = range.substring(6).split("-");
                long start = 0;
                long end = fileSize - 1;
                
                if (ranges.length > 0 && !ranges[0].isEmpty()) {
                    start = Long.parseLong(ranges[0]);
                }
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
                
                // 범위 검증
                start = Math.max(0, start);
                end = Math.min(fileSize - 1, end);
                
                if (start <= end) {
                    int length = (int) (end - start + 1);
                    byte[] buffer = new byte[length];
                    
                    try (var channel = Files.newByteChannel(filePath)) {
                        channel.position(start);
                        var byteBuffer = java.nio.ByteBuffer.wrap(buffer);
                        channel.read(byteBuffer);
                    }
                    
                    return buffer;
                }
            }
            
            // 기본적으로 전체 파일 반환
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            log.error("파일 스트리밍 실패: {}", file.getName(), e);
            throw new RuntimeException("파일 스트리밍 중 오류가 발생했습니다", e);
        }
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
