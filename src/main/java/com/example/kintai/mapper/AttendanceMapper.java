package com.example.kintai.mapper;

import com.example.kintai.entity.Attendance;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AttendanceMapper {

    @Select("SELECT * FROM attendance WHERE employee_id = #{employeeId} AND work_date = #{workDate}")
    Attendance findByEmployeeIdAndDate(@Param("employeeId") Long employeeId,
                                       @Param("workDate") LocalDate workDate);

    @Insert("INSERT INTO attendance (employee_id, work_date, clock_in) " +
            "VALUES (#{employeeId}, #{workDate}, #{clockIn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Attendance attendance);

    @Update("UPDATE attendance SET clock_out = #{clockOut} WHERE id = #{id} AND clock_out IS NULL")
    int updateClockOut(@Param("id") Long id, @Param("clockOut") LocalDateTime clockOut);

    @Select("SELECT * FROM attendance WHERE employee_id = #{employeeId} " +
            "AND work_date BETWEEN #{from} AND #{to} ORDER BY work_date DESC")
    List<Attendance> findByEmployeeIdAndPeriod(@Param("employeeId") Long employeeId,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);
}