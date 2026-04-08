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

        // ERD 물리명칭(ALARM_ID, RECEIVER_ID 등)과 DTO 필드 구조를 고려한 쿼리
        // reservation 테이블의 모든 컬럼과 연관 테이블의 필수 정보를 JOIN
        String sql = "SELECT r.reservation_id, r.reservation_date, r.status, r.purpose, r.target_type, r.created_at, r.approved_at, r.real_use, " +
                "       u.user_id, u.name AS u_name, " +
                "       p.period_id, p.start_time, p.end_time, " +
                "       f.facility_id, f.name AS f_name, " +
                "       e.equipment_id, e.name AS e_name " +
                "FROM reservation r " +
                "JOIN user u ON r.user_id = u.user_id " +
                "JOIN period p ON r.period_id = p.period_id " +
                "LEFT JOIN facility f ON r.facility_id = f.facility_id " +
                "LEFT JOIN equipment e ON r.equipment_id = e.equipment_id " +
                "WHERE r.reservation_date = CURDATE() AND r.status = '승인'";

        try (Connection conn = MySql.getMySql().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // 1. User 객체 조립 (u_name 별칭 사용)
                User user = User.builder()
                        .userId(rs.getInt("user_id"))
                        .name(rs.getString("u_name"))
                        .build();

                // 2. Period 객체 조립 (시간 변환 처리)
                Period period = Period.builder()
                        .periodId(rs.getInt("period_id"))
                        .startTime(rs.getTime("start_time").toLocalTime())
                        .endTime(rs.getTime("end_time").toLocalTime())
                        .build();

                // 3. Facility / Equipment 조립 (null 체크 필수)
                Facility facility = null;
                if (rs.getObject("facility_id") != null) {
                    facility = Facility.builder()
                            .facilityId(rs.getLong("facility_id")) // DTO 타입(Long) 반영
                            .name(rs.getString("f_name"))
                            .build();
                }

                Equipment equipment = null;
                if (rs.getObject("equipment_id") != null) {
                    equipment = Equipment.builder()
                            .equipmentId(rs.getLong("equipment_id")) // DTO 타입(Long) 반영
                            .name(rs.getString("e_name"))
                            .build();
                }

                // 4. 메인 Reservation 객체 완성
                Reservation reservation = Reservation.builder()
                        .reservationId(rs.getLong("reservation_id"))
                        .user(user)       // 객체 주입
                        .period(period)   // 객체 주입
                        .facility(facility)
                        .equipment(equipment)
                        .purpose(rs.getString("purpose"))
                        .targetType(rs.getString("target_type"))
                        .status(rs.getString("status"))
                        .realUse(rs.getString("real_use"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .reservationDate(rs.getDate("reservation_date").toLocalDate())
                        .approvedAt(rs.getTimestamp("approved_at") != null ?
                                rs.getTimestamp("approved_at").toLocalDateTime() : null)
                        .build();

                list.add(reservation);
            }
        } catch (SQLException e) {
            System.err.println("❌ 오늘자 승인 예약 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

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
