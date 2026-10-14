package com.example.kintai.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Employee {
    private Long id;
    private String employeeCode;
    private String name;
    private String password;
    private String role;
    private LocalDateTime createdAt;
}