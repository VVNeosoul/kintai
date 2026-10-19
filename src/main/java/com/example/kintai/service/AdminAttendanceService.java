package com.example.kintai.service;

import com.example.kintai.dto.AttendanceRow;
import com.example.kintai.dto.PageResult;
import com.example.kintai.mapper.AttendanceMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminAttendanceService {

    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final AttendanceMapper attendanceMapper;

    public AdminAttendanceService(AttendanceMapper attendanceMapper) {
        this.attendanceMapper = attendanceMapper;
    }

    public PageResult<AttendanceRow> search(YearMonth month, Long employeeId, int page) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        long total = attendanceMapper.count(from, to, employeeId);
        int totalPages = (int) Math.max(1, Math.ceil(total / (double) PAGE_SIZE));
        int current = Math.min(Math.max(page, 1), totalPages);   // 範囲外のページ番号を補正
        int offset = (current - 1) * PAGE_SIZE;

        List<AttendanceRow> rows = attendanceMapper.search(from, to, employeeId, PAGE_SIZE, offset);
        return new PageResult<>(rows, current, totalPages, total);
    }

    public byte[] exportCsv(YearMonth month, Long employeeId) {
        // エクスポートはページングしないので、十分大きな上限を渡す
        List<AttendanceRow> rows = attendanceMapper.search(
                month.atDay(1), month.atEndOfMonth(), employeeId, Integer.MAX_VALUE, 0);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');   // BOM：Excelで日本語が文字化けしないようにする
        sb.append("社員番号,氏名,日付,出勤,退勤,勤務時間\r\n");
        for (AttendanceRow r : rows) {
            sb.append(escape(r.getEmployeeCode())).append(',')
                    .append(escape(r.getEmployeeName())).append(',')
                    .append(r.getWorkDate()).append(',')
                    .append(r.getClockIn() != null ? r.getClockIn().format(TIME) : "").append(',')
                    .append(r.getClockOut() != null ? r.getClockOut().format(TIME) : "").append(',')
                    .append(r.getWorkingTime())
                    .append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}