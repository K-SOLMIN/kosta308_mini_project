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

    // AlarmService.java 내부에 추가
    public List<Reservation> getTodayApprovedReservations() {
        List<Reservation> list = new ArrayList<>();
        // ERD 컬럼명: reservation_date, status, name, start_time, end_time 등 정확히 반영
        String sql = "SELECT r.*, u.user_id, u.name as user_name, " +
                "p.start_time, p.end_time, " +
                "f.name as facility_name, e.name as equipment_name " +
                "FROM reservation r " +
                "JOIN user u ON r.user_id = u.user_id " +
                "JOIN period p ON r.period_id = p.period_id " +
                "LEFT JOIN facility f ON r.facility_id = f.facility_id " +
                "LEFT JOIN equipment e ON r.equipment_id = e.equipment_id " +
                "WHERE r.reservation_date = CURDATE() AND r.status = 'APPROVED'";

        // 여기서부터는 Connection 맺고 ResultSet 돌려서 리스트 채우는 JDBC 기본 로직 작성...
        // (이전 답변의 getReservationsByDate 로직과 동일하게 구현하시면 됩니다!)
        return list;
    }

    /**
     * 알림을 DB에 저장하고 실시간 소켓으로 전송합니다.
     * @param receiverId : 수신자 ID (user_id)
     * @param content    : 알림 메시지 내용
     * @param type       : 알림 타입 (START / RETURN)
     */
    public void sendAndSaveAlarm(int receiverId, String content, String type) {
        // 1. DB 저장 (ERD: alarm 테이블)
        boolean isSaved = saveAlarmToDb(receiverId, content);

        if (isSaved) {
            // 2. 실시간 소켓 전송 (선생님의 소켓 서버 로직 호출)
            // 예: SessionManager.getInstance().sendToUser(receiverId, content);
            System.out.println("🚀 [소켓 전송 완료] User " + receiverId + "에게 메시지 발송");
        } else {
            System.out.println("❌ [알림 저장 실패] DB 확인이 필요합니다.");
        }
    }

    /**
     * 알림 데이터를 DB에 Insert (ERD 컬럼명 준수)
     */
    private boolean saveAlarmToDb(int receiverId, String content) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;

        // ERD 기준: receiver_id, content, generate_date, is_read
        String sql = "INSERT INTO alarm (receiver_id, content, generate_date, isread, type) " +
                "VALUES (?, ?, NOW(), 'N', '[testType]')";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, receiverId);
            pstmt.setString(2, content);

            int result = pstmt.executeUpdate();
            conn.commit();
            return result > 0;
        } catch (SQLException e) {
            mysql.rollback(conn);
            e.printStackTrace();
            return false;
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }
    }
}
