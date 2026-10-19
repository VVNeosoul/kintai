package com.example.kintai.mapper;

import com.example.kintai.dto.AttendanceRow;
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

    @Select("""
        <script>
        SELECT e.employee_code AS employee_code, e.name AS employee_name,
               a.work_date, a.clock_in, a.clock_out
        FROM attendance a
        JOIN employees e ON a.employee_id = e.id
        WHERE a.work_date BETWEEN #{from} AND #{to}
        <if test="employeeId != null">
            AND a.employee_id = #{employeeId}
        </if>
        ORDER BY a.work_date DESC, e.employee_code
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<AttendanceRow> search(@Param("from") LocalDate from,
                               @Param("to") LocalDate to,
                               @Param("employeeId") Long employeeId,
                               @Param("limit") int limit,
                               @Param("offset") int offset);

    @Select("""
        <script>
        SELECT COUNT(*) FROM attendance a
        WHERE a.work_date BETWEEN #{from} AND #{to}
        <if test="employeeId != null">
            AND a.employee_id = #{employeeId}
        </if>
        </script>
        """)
    long count(@Param("from") LocalDate from,
               @Param("to") LocalDate to,
               @Param("employeeId") Long employeeId);
}