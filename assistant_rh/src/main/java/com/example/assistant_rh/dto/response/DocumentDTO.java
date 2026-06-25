package com.example.assistant_rh.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String contentType;
    private Long fileSize;
    private String documentCategory;
    private String description;
    private LocalDateTime uploadedAt;
    private Long employeeId;
    private String employeeFullName;
    private String uploadedByEmail;
    private String downloadUrl;
}
