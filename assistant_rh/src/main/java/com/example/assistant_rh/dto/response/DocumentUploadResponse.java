package com.example.assistant_rh.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUploadResponse {
    // Information about the uploaded document
    private Long id;
    private String fileName;
    private String fileType;
    private String contentType;
    private Long fileSize;
    private String fileSizeFormatted;
    private String documentCategory;
    private String description;
    private LocalDateTime uploadedAt;
    private String downloadUrl;

    // Success or error message 
    private boolean success;
    private String message;
    private String employeeName;
}
