package com.example.kintai.controller;

import com.example.kintai.exception.LeaveException;
import com.example.kintai.form.LeaveRequestForm;
import com.example.kintai.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public String index(Authentication authentication, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new LeaveRequestForm());
        }
        model.addAttribute("records", leaveService.listMine(authentication.getName()));
        return "leave/index";
    }

    @PostMapping
    public String apply(@Valid @ModelAttribute("form") LeaveRequestForm form,
                        BindingResult result,
                        Authentication authentication,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        String code = authentication.getName();

        if (!result.hasErrors()) {
            try {
                leaveService.apply(code, form);
                redirectAttributes.addFlashAttribute("message", "休暇を申請しました。");
                return "redirect:/leave";
            } catch (LeaveException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        // 入力エラー・業務エラー時は、入力内容を保持したまま同じ画面を再表示する
        model.addAttribute("records", leaveService.listMine(code));
        return "leave/index";
    }
}