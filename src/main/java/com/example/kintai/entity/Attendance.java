package com.example.kintai.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Attendance {
    private Long id;
    private Long employeeId;
    private LocalDate workDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
}