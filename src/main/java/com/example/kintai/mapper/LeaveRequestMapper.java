package com.example.kintai.mapper;

import com.example.kintai.entity.LeaveRequest;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface LeaveRequestMapper {

    @Insert("INSERT INTO leave_requests (employee_id, leave_date, reason) " +
            "VALUES (#{employeeId}, #{leaveDate}, #{reason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LeaveRequest request);

    @Select("SELECT COUNT(*) FROM leave_requests " +
            "WHERE employee_id = #{employeeId} AND leave_date = #{leaveDate} AND status <> 'REJECTED'")
    int countActiveByDate(@Param("employeeId") Long employeeId,
                          @Param("leaveDate") LocalDate leaveDate);

    @Select("SELECT * FROM leave_requests WHERE employee_id = #{employeeId} ORDER BY leave_date DESC")
    List<LeaveRequest> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Select("SELECT l.*, e.name AS employee_name FROM leave_requests l " +
            "JOIN employees e ON l.employee_id = e.id " +
            "WHERE l.status = 'PENDING' ORDER BY l.leave_date, l.id")
    List<LeaveRequest> findPending();

    @Select("SELECT * FROM leave_requests WHERE id = #{id}")
    LeaveRequest findById(@Param("id") Long id);

    @Update("UPDATE leave_requests SET status = #{status} WHERE id = #{id} AND status = 'PENDING'")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}