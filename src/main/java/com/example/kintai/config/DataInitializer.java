package com.example.kintai.config;

import com.example.kintai.entity.Employee;
import com.example.kintai.mapper.EmployeeMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// 開発用のデモアカウントを作成する（本番では使わない）
@Component
public class DataInitializer implements ApplicationRunner {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeMapper employeeMapper, PasswordEncoder passwordEncoder) {
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfAbsent("E001", "山田太郎", "ADMIN");
        createIfAbsent("E002", "李明", "EMPLOYEE");
    }

    private void createIfAbsent(String code, String name, String role) {
        if (employeeMapper.findByEmployeeCode(code) == null) {
            Employee e = new Employee();
            e.setEmployeeCode(code);
            e.setName(name);
            e.setPassword(passwordEncoder.encode("password123"));
            e.setRole(role);
            employeeMapper.insert(e);
        }
    }
}