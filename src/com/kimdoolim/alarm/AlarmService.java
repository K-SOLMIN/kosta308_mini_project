package com.kimdoolim.alarm;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AlarmService {
    Database mysql = MySql.getMySql();

    public List<Reservation> getReservationsByDate(LocalDate date) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Reservation> list = new ArrayList<>();

        // JOIN을 통해 사용자명, 교시정보, 시설/비품명을 한 번에 가져옵니다.
        // 상태가 'APPROVED'(승인)인 건만 가져와야 반납 알림의 의미가 있습니다.
        String sql = "SELECT r.*, u.name, p.period_name, p.start_time, p.end_time, " +
                "f.name, e.name " +
                "FROM reservation r " +
                "JOIN user u ON r.user_id = u.user_id " +
                "JOIN period p ON r.period_id = p.period_id " +
                "LEFT JOIN facility f ON r.facility_id = f.facility_id " +
                "LEFT JOIN equipment e ON r.equipment_id = e.equipment_id " +
                "WHERE r.reservation_date = ? AND r.status = 'APPROVED'";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            rs = pstmt.executeQuery();

            while (rs.next()) {
                // 1. User 정보 세팅
                User user = User.builder()
                        .name(rs.getString("name"))
                        .build();

                // 2. Period 정보 세팅 (LocalTime 변환)
                Period period = Period.builder()
                        .periodName(rs.getString("period_name"))
                        .startTime(rs.getTime("start_time").toLocalTime())
                        .endTime(rs.getTime("end_time").toLocalTime())
                        .build();

                // 3. 시설/비품 정보 세팅
                Facility facility = null;
                if (rs.getString("name") != null) {
                    facility = Facility.builder().name(rs.getString("name")).build();
                }

                Equipment equipment = null;
                if (rs.getString("name") != null) {
                    equipment = Equipment.builder().name(rs.getString("name")).build();
                }

                // 4. 최종 Reservation 객체 조립
                Reservation res = Reservation.builder()
                        .reservationId(rs.getInt("reservation_id"))
                        .user(user)
                        .period(period)
                        .facility(facility)
                        .equipment(equipment)
                        .targetType(rs.getString("target_type")) // "FACILITY" 또는 "EQUIPMENT"
                        .reservationDate(rs.getDate("reservation_date").toLocalDate())
                        .status(rs.getString("status"))
                        .build();

                list.add(res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.close(rs);
            mysql.close(pstmt);
            mysql.close(conn);
        }
        return list;
    }
}
