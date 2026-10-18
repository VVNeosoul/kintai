package com.example.kintai.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class LeaveRequestForm {

    @NotNull(message = "休暇日を入力してください。")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate leaveDate;

    @NotBlank(message = "理由を入力してください。")
    @Size(max = 200, message = "理由は200文字以内で入力してください。")
    private String reason;
}