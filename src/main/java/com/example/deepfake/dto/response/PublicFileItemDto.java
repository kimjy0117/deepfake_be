package com.example.deepfake.dto.response;

import com.example.deepfake.file.dto.FileItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicFileItemDto extends FileItemDto {
    
    private String userName;
    
    public static PublicFileItemDtoBuilder publicBuilder() {
        return new PublicFileItemDtoBuilder();
    }
    
    public static class PublicFileItemDtoBuilder {
        private Long id;
        private String name;
        private String title;
        private String originalName;
        private String url;
        private String thumbnailUrl;
        private Long size;
        private com.example.deepfake.file.entity.File.FileType type;
        private String mimeType;
        private Long userId;
        private java.time.LocalDateTime uploadedAt;
        private String userName;
        
        public PublicFileItemDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public PublicFileItemDtoBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public PublicFileItemDtoBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public PublicFileItemDtoBuilder originalName(String originalName) {
            this.originalName = originalName;
            return this;
        }
        
        public PublicFileItemDtoBuilder url(String url) {
            this.url = url;
            return this;
        }
        
        public PublicFileItemDtoBuilder thumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }
        
        public PublicFileItemDtoBuilder size(Long size) {
            this.size = size;
            return this;
        }
        
        public PublicFileItemDtoBuilder type(com.example.deepfake.file.entity.File.FileType type) {
            this.type = type;
            return this;
        }
        
        public PublicFileItemDtoBuilder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }
        
        public PublicFileItemDtoBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public PublicFileItemDtoBuilder uploadedAt(java.time.LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
            return this;
        }
        
        public PublicFileItemDtoBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }
        
        public PublicFileItemDto build() {
            PublicFileItemDto dto = new PublicFileItemDto();
            dto.setId(this.id);
            dto.setName(this.name);
            dto.setTitle(this.title);
            dto.setOriginalName(this.originalName);
            dto.setUrl(this.url);
            dto.setThumbnailUrl(this.thumbnailUrl);
            dto.setSize(this.size);
            dto.setType(this.type);
            dto.setMimeType(this.mimeType);
            dto.setUserId(this.userId);
            dto.setUploadedAt(this.uploadedAt);
            dto.setUserName(this.userName);
            return dto;
        }
    }
}
