package com.example.kintai.config;

import com.example.kintai.entity.Employee;
import com.example.kintai.mapper.EmployeeMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeMapper employeeMapper;

    public CustomUserDetailsService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeMapper.findByEmployeeCode(username);
        if (employee == null) {
            throw new UsernameNotFoundException("社員が見つかりません: " + username);
        }
        return User.withUsername(employee.getEmployeeCode())
                .password(employee.getPassword())
                .roles(employee.getRole())
                .build();
    }
}