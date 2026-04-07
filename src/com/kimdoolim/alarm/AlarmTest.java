package com.kimdoolim.alarm;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.Period;
import com.kimdoolim.dto.Reservation;
import com.kimdoolim.dto.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AlarmTest {
    private final Database db = MySql.getMySql();

    /**
     * 예약 등록 + 자동 승인 + [현재시간 + 11분]으로 알림 예약 호출
     */
    public void registerAndApprove() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long resId = -1;

        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            // [Step 1] 예약 DB 등록 (status: 대기)
            String sqlInsert = "INSERT INTO reservation " +
                    "(period_id, user_id, facility_id, purpose, created_at, reservation_date, status, real_use, target_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, '대기', 'false', 'FACILITY')";

            pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 1);
            pstmt.setLong(3, 1);
            pstmt.setString(4, "[알림테스트]");
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setDate(6, Date.valueOf(LocalDate.now()));

            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) resId = rs.getLong(1);

            db.close(pstmt);

            // [Step 2] 즉시 승인 처리 (Update)
            String sqlUpdate = "UPDATE reservation SET status = '승인', approved_at = NOW() WHERE reservation_id = ?";
            pstmt = conn.prepareStatement(sqlUpdate);
            pstmt.setLong(1, resId);
            pstmt.executeUpdate();

            conn.commit();
            System.out.println("✅ DB 등록/승인 완료 (ID: " + resId + ")");

            // [Step 3] 핵심: 현재 시간 기준 +11분 뒤로 '시작 시간' 조작하여 스케줄러 호출
            Reservation approvedRes = fetchFullReservation(resId);

            if (approvedRes != null) {
                // 테스트를 위해 시작 시간을 현재 시간 + 11분으로 강제 세팅
                // 이렇게 하면 스케줄러 내부의 (시작시간 - 10분) 로직에 의해 1분 뒤에 알림이 울립니다.
                LocalTime testStartTime = LocalTime.now().plusMinutes(11);
                approvedRes.getPeriod().setStartTime(testStartTime);

                System.out.println("⏰ 테스트 설정: 현재시간은 " + LocalTime.now() + " / 예약시작은 " + testStartTime);
                System.out.println("🔔 알림은 약 1분 뒤인 " + testStartTime.minusMinutes(10) + "에 울릴 예정입니다.");

                // 스케줄러 호출
                AlarmScheduler.getInstance().addReservationAlarm(approvedRes);
            }

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
        } finally {
            db.close(rs); db.close(pstmt); db.close(conn);
        }
    }

    private Reservation fetchFullReservation(long resId) {
        Connection conn = db.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql = "SELECT r.*, u.name AS user_name, p.start_time, p.end_time, f.name AS facility_name " +
                "FROM reservation r " +
                "JOIN user u ON r.user_id = u.user_id " +
                "JOIN period p ON r.period_id = p.period_id " +
                "LEFT JOIN facility f ON r.facility_id = f.facility_id " +
                "WHERE r.reservation_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, resId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = User.builder().userId(rs.getInt("user_id")).name(rs.getString("user_name")).build();
                // DB의 실제 교시 시간 정보를 가져온 뒤 나중에 위에서 덮어씌움
                Period period = Period.builder()
                        .startTime(rs.getTime("start_time").toLocalTime())
                        .endTime(rs.getTime("end_time").toLocalTime()).build();
                Facility facility = Facility.builder().name(rs.getString("facility_name")).build();

                return Reservation.builder()
                        .reservationId(rs.getLong("reservation_id"))
                        .user(user).period(period).facility(facility)
                        .targetType("FACILITY").reservationDate(rs.getDate("reservation_date").toLocalDate())
                        .build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { db.close(rs); db.close(pstmt); db.close(conn); }
        return null;
    }
}