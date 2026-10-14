package com.example.kintai.controller;

import com.example.kintai.entity.Employee;
import com.example.kintai.mapper.EmployeeMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class EmployeeController {

    private final EmployeeMapper employeeMapper;

    public EmployeeController(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @GetMapping("/employees")
    public List<Employee> list() {
        return employeeMapper.findAll();
    }
}