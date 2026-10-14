package com.example.kintai.mapper;

import com.example.kintai.entity.Employee;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface EmployeeMapper {

    @Select("SELECT * FROM employees ORDER BY id")
    List<Employee> findAll();

    @Select("SELECT * FROM employees WHERE employee_code = #{employeeCode}")
    Employee findByEmployeeCode(@Param("employeeCode") String employeeCode);

    @Insert("INSERT INTO employees (employee_code, name, password, role) " +
            "VALUES (#{employeeCode}, #{name}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Employee employee);
}