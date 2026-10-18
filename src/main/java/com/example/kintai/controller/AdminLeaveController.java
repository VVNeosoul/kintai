package com.example.kintai.controller;

import com.example.kintai.exception.LeaveException;
import com.example.kintai.service.LeaveService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/leave")
public class AdminLeaveController {

    private final LeaveService leaveService;

    public AdminLeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("requests", leaveService.listPending());
        return "admin/leave";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            leaveService.approve(id, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "承認しました。");
        } catch (LeaveException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leave";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            leaveService.reject(id, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "却下しました。");
        } catch (LeaveException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leave";
    }
}