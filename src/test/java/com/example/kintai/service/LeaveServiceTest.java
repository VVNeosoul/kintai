package com.example.kintai.service;

import com.example.kintai.entity.Employee;
import com.example.kintai.entity.LeaveRequest;
import com.example.kintai.exception.LeaveException;
import com.example.kintai.form.LeaveRequestForm;
import com.example.kintai.mapper.EmployeeMapper;
import com.example.kintai.mapper.LeaveRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");

    @Mock
    private LeaveRequestMapper leaveMapper;
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private LeaveService service;

    @BeforeEach
    void setUp() {
        lenient().when(employeeMapper.findByEmployeeCode("E002"))
                .thenReturn(employee(1L, "E002", "EMPLOYEE"));
        lenient().when(employeeMapper.findByEmployeeCode("E001"))
                .thenReturn(employee(2L, "E001", "ADMIN"));
    }

    private Employee employee(Long id, String code, String role) {
        Employee e = new Employee();
        e.setId(id);
        e.setEmployeeCode(code);
        e.setRole(role);
        return e;
    }

    private LeaveRequestForm form(LocalDate date) {
        LeaveRequestForm f = new LeaveRequestForm();
        f.setLeaveDate(date);
        f.setReason("私用のため");
        return f;
    }

    private LeaveRequest request(Long id, Long employeeId) {
        LeaveRequest r = new LeaveRequest();
        r.setId(id);
        r.setEmployeeId(employeeId);
        r.setStatus("PENDING");
        return r;
    }

    @Test
    @DisplayName("申請：未来の日付で未申請なら登録される")
    void apply_success() {
        LocalDate tomorrow = LocalDate.now(ZONE).plusDays(1);
        when(leaveMapper.countActiveByDate(1L, tomorrow)).thenReturn(0);

        service.apply("E002", form(tomorrow));

        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveMapper).insert(captor.capture());
        assertThat(captor.getValue().getEmployeeId()).isEqualTo(1L);
        assertThat(captor.getValue().getLeaveDate()).isEqualTo(tomorrow);
        assertThat(captor.getValue().getReason()).isEqualTo("私用のため");
    }

    @Test
    @DisplayName("申請：過去の日付はエラーになり、保存されない")
    void apply_pastDate() {
        LocalDate yesterday = LocalDate.now(ZONE).minusDays(1);

        assertThatThrownBy(() -> service.apply("E002", form(yesterday)))
                .isInstanceOf(LeaveException.class)
                .hasMessageContaining("過去の日付");
        verify(leaveMapper, never()).insert(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("申請：同じ日にすでに有効な申請があればエラーになる")
    void apply_duplicateDate() {
        LocalDate tomorrow = LocalDate.now(ZONE).plusDays(1);
        when(leaveMapper.countActiveByDate(1L, tomorrow)).thenReturn(1);

        assertThatThrownBy(() -> service.apply("E002", form(tomorrow)))
                .isInstanceOf(LeaveException.class)
                .hasMessageContaining("すでに申請済み");
        verify(leaveMapper, never()).insert(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("承認：他人の申請を承認できる")
    void approve_success() {
        when(leaveMapper.findById(5L)).thenReturn(request(5L, 1L));
        when(leaveMapper.updateStatus(5L, "APPROVED")).thenReturn(1);

        service.approve(5L, "E001");

        verify(leaveMapper).updateStatus(5L, "APPROVED");
    }

    @Test
    @DisplayName("却下：他人の申請を却下できる")
    void reject_success() {
        when(leaveMapper.findById(5L)).thenReturn(request(5L, 1L));
        when(leaveMapper.updateStatus(5L, "REJECTED")).thenReturn(1);

        service.reject(5L, "E001");

        verify(leaveMapper).updateStatus(5L, "REJECTED");
    }

    @Test
    @DisplayName("承認：自分の申請は承認できない")
    void approve_ownRequest() {
        when(leaveMapper.findById(5L)).thenReturn(request(5L, 2L));   // 申請者 = 管理者本人

        assertThatThrownBy(() -> service.approve(5L, "E001"))
                .isInstanceOf(LeaveException.class)
                .hasMessageContaining("自分の申請");
        verify(leaveMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    @DisplayName("承認：すでに処理済みの申請はエラーになる")
    void approve_alreadyProcessed() {
        when(leaveMapper.findById(5L)).thenReturn(request(5L, 1L));
        when(leaveMapper.updateStatus(5L, "APPROVED")).thenReturn(0);

        assertThatThrownBy(() -> service.approve(5L, "E001"))
                .isInstanceOf(LeaveException.class)
                .hasMessageContaining("処理済み");
    }

    @Test
    @DisplayName("承認：存在しない申請はエラーになる")
    void approve_notFound() {
        when(leaveMapper.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.approve(99L, "E001"))
                .isInstanceOf(LeaveException.class)
                .hasMessageContaining("申請が見つかりません");
    }
}