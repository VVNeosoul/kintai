package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.Employee;
import com.example.kintai.exception.AttendanceException;
import com.example.kintai.mapper.AttendanceMapper;
import com.example.kintai.mapper.EmployeeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceMapper attendanceMapper;
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private AttendanceService service;

    @BeforeEach
    void setUp() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("E002");
        // テストによっては使わないので lenient にしておく
        lenient().when(employeeMapper.findByEmployeeCode("E002")).thenReturn(employee);
    }

    private Attendance attendance(Long id, LocalDateTime clockOut) {
        Attendance a = new Attendance();
        a.setId(id);
        a.setEmployeeId(1L);
        a.setWorkDate(LocalDate.now());
        a.setClockIn(LocalDateTime.now());
        a.setClockOut(clockOut);
        return a;
    }

    @Test
    @DisplayName("出勤打刻：未打刻なら記録が作成される")
    void clockIn_success() {
        when(attendanceMapper.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(null);

        service.clockIn("E002");

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceMapper).insert(captor.capture());
        Attendance saved = captor.getValue();
        assertThat(saved.getEmployeeId()).isEqualTo(1L);
        assertThat(saved.getClockIn()).isNotNull();
        assertThat(saved.getWorkDate()).isEqualTo(saved.getClockIn().toLocalDate());
    }

    @Test
    @DisplayName("出勤打刻：同じ日に2回目はエラーになり、保存されない")
    void clockIn_alreadyClockedIn() {
        when(attendanceMapper.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(attendance(10L, null));

        assertThatThrownBy(() -> service.clockIn("E002"))
                .isInstanceOf(AttendanceException.class)
                .hasMessageContaining("すでに出勤打刻済み");
        verify(attendanceMapper, never()).insert(any(Attendance.class));
    }

    @Test
    @DisplayName("出勤打刻：同時リクエストでDBの一意制約に当たっても業務エラーに変換される")
    void clockIn_dbUniqueConstraintViolation() {
        when(attendanceMapper.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(null);
        doThrow(new DuplicateKeyException("duplicate")).when(attendanceMapper).insert(any(Attendance.class));

        assertThatThrownBy(() -> service.clockIn("E002"))
                .isInstanceOf(AttendanceException.class)
                .hasMessageContaining("すでに出勤打刻済み");
    }

    @Test
    @DisplayName("出勤打刻：存在しない社員はエラーになる")
    void clockIn_unknownEmployee() {
        assertThatThrownBy(() -> service.clockIn("E999"))
                .isInstanceOf(AttendanceException.class)
                .hasMessageContaining("社員情報が見つかりません");
        verify(attendanceMapper, never()).insert(any(Attendance.class));
    }

    @Test
    @DisplayName("退勤打刻：出勤済みなら退勤時刻が更新される")
    void clockOut_success() {
        when(attendanceMapper.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(attendance(10L, null));
        when(attendanceMapper.updateClockOut(eq(10L), any(LocalDateTime.class))).thenReturn(1);

        service.clockOut("E002");

        verify(attendanceMapper).updateClockOut(eq(10L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("退勤打刻：出勤打刻がなければエラーになる")
    void clockOut_withoutClockIn() {
        when(attendanceMapper.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(null);

        assertThatThrownBy(() -> service.clockOut("E002"))
                .isInstanceOf(AttendanceException.class)
                .hasMessageContaining("出勤打刻がありません");
        verify(attendanceMapper, never()).updateClockOut(anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("退勤打刻：すでに退勤済みならエラーになる")
    void clockOut_alreadyClockedOut() {
        when(attendanceMapper.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(attendance(10L, LocalDateTime.now()));

        assertThatThrownBy(() -> service.clockOut("E002"))
                .isInstanceOf(AttendanceException.class)
                .hasMessageContaining("すでに退勤打刻済み");
        verify(attendanceMapper, never()).updateClockOut(anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("退勤打刻：同時更新で更新件数が0ならエラーになる")
    void clockOut_concurrentUpdate() {
        when(attendanceMapper.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(attendance(10L, null));
        when(attendanceMapper.updateClockOut(eq(10L), any(LocalDateTime.class))).thenReturn(0);

        assertThatThrownBy(() -> service.clockOut("E002"))
                .isInstanceOf(AttendanceException.class)
                .hasMessageContaining("すでに退勤打刻済み");
    }
}