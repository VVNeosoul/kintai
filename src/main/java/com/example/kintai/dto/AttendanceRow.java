package com.example.kintai.dto;

import lombok.Data;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceRow {
    private String employeeCode;
    private String employeeName;
    private LocalDate workDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    // 勤務時間（休憩は考慮しない単純な差）
    public String getWorkingTime() {
        if (clockIn == null || clockOut == null) {
            return "-";
        }
        Duration d = Duration.between(clockIn, clockOut);
        return String.format("%d:%02d", d.toHours(), d.toMinutesPart());
    }
}