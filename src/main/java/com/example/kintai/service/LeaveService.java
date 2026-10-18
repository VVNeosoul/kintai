package com.example.kintai.service;

import com.example.kintai.entity.Employee;
import com.example.kintai.entity.LeaveRequest;
import com.example.kintai.exception.LeaveException;
import com.example.kintai.form.LeaveRequestForm;
import com.example.kintai.mapper.EmployeeMapper;
import com.example.kintai.mapper.LeaveRequestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class LeaveService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");

    private final LeaveRequestMapper leaveMapper;
    private final EmployeeMapper employeeMapper;

    public LeaveService(LeaveRequestMapper leaveMapper, EmployeeMapper employeeMapper) {
        this.leaveMapper = leaveMapper;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public void apply(String employeeCode, LeaveRequestForm form) {
        Employee employee = getEmployee(employeeCode);

        if (form.getLeaveDate().isBefore(LocalDate.now(ZONE))) {
            throw new LeaveException("過去の日付は申請できません。");
        }
        if (leaveMapper.countActiveByDate(employee.getId(), form.getLeaveDate()) > 0) {
            throw new LeaveException("この日はすでに申請済みです。");
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(employee.getId());
        request.setLeaveDate(form.getLeaveDate());
        request.setReason(form.getReason());
        leaveMapper.insert(request);
    }

    public List<LeaveRequest> listMine(String employeeCode) {
        return leaveMapper.findByEmployeeId(getEmployee(employeeCode).getId());
    }

    public List<LeaveRequest> listPending() {
        return leaveMapper.findPending();
    }

    @Transactional
    public void approve(Long id, String adminCode) {
        decide(id, adminCode, "APPROVED");
    }

    @Transactional
    public void reject(Long id, String adminCode) {
        decide(id, adminCode, "REJECTED");
    }

    private void decide(Long id, String adminCode, String newStatus) {
        Employee admin = getEmployee(adminCode);
        LeaveRequest request = leaveMapper.findById(id);

        if (request == null) {
            throw new LeaveException("申請が見つかりません。");
        }
        if (request.getEmployeeId().equals(admin.getId())) {
            throw new LeaveException("自分の申請は承認・却下できません。");
        }
        if (leaveMapper.updateStatus(id, newStatus) == 0) {
            throw new LeaveException("この申請はすでに処理済みです。");
        }
    }

    private Employee getEmployee(String employeeCode) {
        Employee employee = employeeMapper.findByEmployeeCode(employeeCode);
        if (employee == null) {
            throw new LeaveException("社員情報が見つかりません。");
        }
        return employee;
    }
}