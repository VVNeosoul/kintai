package com.example.kintai.controller;

import com.example.kintai.exception.AttendanceException;
import com.example.kintai.service.AttendanceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        String code = authentication.getName();
        model.addAttribute("today", attendanceService.getToday(code));
        model.addAttribute("records", attendanceService.getThisMonth(code));
        return "attendance/index";
    }

    @PostMapping("/clock-in")
    public String clockIn(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.clockIn(authentication.getName());
            redirectAttributes.addFlashAttribute("message", "出勤を打刻しました。");
        } catch (AttendanceException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/attendance";
    }

    @PostMapping("/clock-out")
    public String clockOut(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.clockOut(authentication.getName());
            redirectAttributes.addFlashAttribute("message", "退勤を打刻しました。");
        } catch (AttendanceException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/attendance";
    }
}