package com.example.assistant_rh.dto.request;

import lombok.Data;

@Data
public class BulkApproveRequest {
    private String adminComment;
    private boolean confirmed = false;
}
