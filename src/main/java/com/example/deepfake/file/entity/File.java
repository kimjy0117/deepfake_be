package com.example.deepfake.file.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class File {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(name = "original_name", nullable = false)
    private String originalName;
    
    @Column(nullable = false)
    private String url;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(nullable = false)
    private Long size;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType type;
    
    @Column(name = "mime_type", nullable = false)
    private String mimeType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.example.deepfake.user.entity.User user;
    
    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum FileType {
        IMAGE, VIDEO
    }
}
