package com.example.kintai.controller;

import com.example.kintai.dto.AttendanceRow;
import com.example.kintai.dto.PageResult;
import com.example.kintai.mapper.EmployeeMapper;
import com.example.kintai.service.AdminAttendanceService;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.ZoneId;

@Controller
@RequestMapping("/admin/attendance")
public class AdminAttendanceController {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");

    private final AdminAttendanceService service;
    private final EmployeeMapper employeeMapper;

    public AdminAttendanceController(AdminAttendanceService service, EmployeeMapper employeeMapper) {
        this.service = service;
        this.employeeMapper = employeeMapper;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String month,
                        @RequestParam(required = false) Long employeeId,
                        @RequestParam(defaultValue = "1") int page,
                        Model model) {
        YearMonth target = parseMonth(month);
        PageResult<AttendanceRow> result = service.search(target, employeeId, page);

        model.addAttribute("result", result);
        model.addAttribute("month", target.toString());
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("employees", employeeMapper.findAll());
        return "admin/attendance";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String month,
                                         @RequestParam(required = false) Long employeeId) {
        YearMonth target = parseMonth(month);
        byte[] csv = service.exportCsv(target, employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance_" + target + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("uuuu-M");

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now(ZONE);
        }
        try {
            return YearMonth.parse(month.trim(), MONTH_FORMAT);
        } catch (DateTimeParseException e) {
            return YearMonth.now(ZONE);
        }
    }
}