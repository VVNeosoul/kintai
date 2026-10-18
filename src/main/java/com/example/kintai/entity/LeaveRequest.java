package com.example.kintai.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequest {
    private Long id;
    private Long employeeId;
    private LocalDate leaveDate;
    private String reason;
    private String status;        // PENDING / APPROVED / REJECTED
    private LocalDateTime createdAt;

    private String employeeName;  // 管理者画面で表示するための項目（JOIN結果用）
}