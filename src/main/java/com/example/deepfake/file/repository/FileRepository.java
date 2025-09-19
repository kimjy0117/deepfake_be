package com.example.deepfake.file.repository;

import com.example.deepfake.file.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    
    Page<File> findByUserId(Long userId, Pageable pageable);
    
    Page<File> findByUserIdAndType(Long userId, com.example.deepfake.file.entity.File.FileType type, Pageable pageable);
    
    @Query("SELECT f FROM File f WHERE f.user.id = :userId AND " +
           "(LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<File> findByUserIdAndKeyword(@Param("userId") Long userId, 
                                     @Param("keyword") String keyword, 
                                     Pageable pageable);
    
    @Query("SELECT f FROM File f WHERE f.user.id = :userId AND f.type = :type AND " +
           "(LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<File> findByUserIdAndTypeAndKeyword(@Param("userId") Long userId, 
                                            @Param("type") com.example.deepfake.file.entity.File.FileType type, 
                                            @Param("keyword") String keyword, 
                                            Pageable pageable);
    
    @Query("SELECT f FROM File f JOIN f.user u WHERE " +
           "(LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<File> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT f FROM File f JOIN f.user u WHERE f.type = :type AND " +
           "(LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<File> findByTypeAndKeyword(@Param("type") com.example.deepfake.file.entity.File.FileType type, 
                                   @Param("keyword") String keyword, 
                                   Pageable pageable);
    
    Page<File> findByType(com.example.deepfake.file.entity.File.FileType type, Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM File f")
    Long countTotalFiles();
    
    @Query("SELECT COUNT(f) FROM File f WHERE f.type = 'IMAGE'")
    Long countImages();
    
    @Query("SELECT COUNT(f) FROM File f WHERE f.type = 'VIDEO'")
    Long countVideos();
    
    @Query("SELECT COUNT(DISTINCT f.user) FROM File f")
    Long countUsers();
    
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM File f")
    Long sumTotalSize();
    
    Optional<File> findByIdAndUserId(Long id, Long userId);
    
    // 사용자별 파일 조회 (업로드 시간 내림차순)
    Page<File> findByUserIdOrderByUploadedAtDesc(Long userId, Pageable pageable);
    
    // 사용자별 타입별 파일 조회 (업로드 시간 내림차순)
    Page<File> findByUserIdAndTypeOrderByUploadedAtDesc(Long userId, com.example.deepfake.file.entity.File.FileType type, Pageable pageable);
    
    // 전체 파일 조회 (업로드 시간 내림차순)
    Page<File> findAllByOrderByUploadedAtDesc(Pageable pageable);
    
    // 타입별 파일 조회 (업로드 시간 내림차순)
    Page<File> findByTypeOrderByUploadedAtDesc(com.example.deepfake.file.entity.File.FileType type, Pageable pageable);
    
    // 제목 또는 원본 파일명으로 검색 (업로드 시간 내림차순)
    Page<File> findByTitleContainingOrOriginalNameContainingOrderByUploadedAtDesc(String title, String originalName, Pageable pageable);
    
    // 타입별 제목 또는 원본 파일명으로 검색 (업로드 시간 내림차순)
    Page<File> findByTypeAndTitleContainingOrTypeAndOriginalNameContainingOrderByUploadedAtDesc(
        com.example.deepfake.file.entity.File.FileType type1, String title,
        com.example.deepfake.file.entity.File.FileType type2, String originalName,
        Pageable pageable);
}
