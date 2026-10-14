package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.Employee;
import com.example.kintai.exception.AttendanceException;
import com.example.kintai.mapper.AttendanceMapper;
import com.example.kintai.mapper.EmployeeMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AttendanceService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");

    private final AttendanceMapper attendanceMapper;
    private final EmployeeMapper employeeMapper;

    public AttendanceService(AttendanceMapper attendanceMapper, EmployeeMapper employeeMapper) {
        this.attendanceMapper = attendanceMapper;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public void clockIn(String employeeCode) {
        Employee employee = getEmployee(employeeCode);
        LocalDateTime now = LocalDateTime.now(ZONE);

        if (attendanceMapper.findByEmployeeIdAndDate(employee.getId(), now.toLocalDate()) != null) {
            throw new AttendanceException("本日はすでに出勤打刻済みです。");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employee.getId());
        attendance.setWorkDate(now.toLocalDate());
        attendance.setClockIn(now);
        try {
            attendanceMapper.insert(attendance);
        } catch (DuplicateKeyException e) {
            // 同時リクエストでチェックをすり抜けた場合は、DBのユニーク制約で防ぐ
            throw new AttendanceException("本日はすでに出勤打刻済みです。");
        }
    }

    @Transactional
    public void clockOut(String employeeCode) {
        Employee employee = getEmployee(employeeCode);
        LocalDateTime now = LocalDateTime.now(ZONE);

        Attendance today = attendanceMapper.findByEmployeeIdAndDate(employee.getId(), now.toLocalDate());
        if (today == null) {
            throw new AttendanceException("出勤打刻がありません。先に出勤打刻をしてください。");
        }
        if (today.getClockOut() != null) {
            throw new AttendanceException("本日はすでに退勤打刻済みです。");
        }
        if (attendanceMapper.updateClockOut(today.getId(), now) == 0) {
            throw new AttendanceException("本日はすでに退勤打刻済みです。");
        }
    }

    public Attendance getToday(String employeeCode) {
        Employee employee = getEmployee(employeeCode);
        return attendanceMapper.findByEmployeeIdAndDate(employee.getId(), LocalDate.now(ZONE));
    }

    public List<Attendance> getThisMonth(String employeeCode) {
        Employee employee = getEmployee(employeeCode);
        LocalDate today = LocalDate.now(ZONE);
        LocalDate from = today.withDayOfMonth(1);
        LocalDate to = today.withDayOfMonth(today.lengthOfMonth());
        return attendanceMapper.findByEmployeeIdAndPeriod(employee.getId(), from, to);
    }

    private Employee getEmployee(String employeeCode) {
        Employee employee = employeeMapper.findByEmployeeCode(employeeCode);
        if (employee == null) {
            throw new AttendanceException("社員情報が見つかりません。");
        }
        return employee;
    }
}