package com.kimdoolim.dao;

import com.kimdoolim.common.DataBase;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

  private final DataBase db = MySql.getMySql();

  // ─────────────────────────────────────────────────────
  // 1. 교시 전체 조회
  // ─────────────────────────────────────────────────────
  public List<Period> findAllPeriods() {
    List<Period> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT period_id, period_name, start_time, end_time FROM period";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(Period.builder()
            .periodId(rs.getInt("period_id"))
            .periodName(rs.getString("period_name"))
            .startTime(rs.getTime("start_time").toLocalTime())
            .endTime(rs.getTime("end_time").toLocalTime())
            .build());
      }
    } catch (SQLException e) {
      System.out.println("교시 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 2. 예약 가능한 시설 조회
  // ─────────────────────────────────────────────────────
  public List<Facility> findAvailableFacilities() {
    List<Facility> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT facility_id, location, name, max_capacity, status " +
        "FROM facility WHERE is_delete = 'false' AND status = '정상'";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(Facility.builder()
            .facilityId(rs.getLong("facility_id"))
            .location(rs.getString("location"))
            .name(rs.getString("name"))
            .maxCapacity(rs.getInt("max_capacity"))
            .status(rs.getString("status"))
            .build());
      }
    } catch (SQLException e) {
      System.out.println("시설 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 3. 예약 가능한 비품 조회
  // ─────────────────────────────────────────────────────
  public List<Equipment> findAvailableEquipments() {
    List<Equipment> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT equipment_id, name, location, status " +
        "FROM equipment WHERE check_delete = 'false' AND status = '정상'";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(Equipment.builder()
            .equipmentId(rs.getLong("equipment_id"))
            .name(rs.getString("name"))
            .location(rs.getString("location"))
            .status(rs.getString("status"))
            .build());
      }
    } catch (SQLException e) {
      System.out.println("비품 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 4. 중복 예약 체크
  // ─────────────────────────────────────────────────────
  public boolean isDuplicateReservation(LocalDate reservationDate,
                                        int periodId,
                                        Long facilityId,
                                        Long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    boolean isDuplicate = false;

    String sql = "SELECT COUNT(*) FROM reservation " +
        "WHERE reservation_date = ? AND period_id = ? " +
        "AND status NOT IN ('거절', '취소') " +
        "AND (" +
        "    (target_type = 'FACILITY' AND facility_id = ?)" +
        "    OR (target_type = 'EQUIPMENT' AND equipment_id = ?)" +
        ")";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setDate(1, Date.valueOf(reservationDate));
      pstmt.setInt(2, periodId);
      if (facilityId != null) pstmt.setLong(3, facilityId);
      else pstmt.setNull(3, Types.BIGINT);
      if (equipmentId != null) pstmt.setLong(4, equipmentId);
      else pstmt.setNull(4, Types.BIGINT);

      rs = pstmt.executeQuery();
      if (rs.next()) isDuplicate = rs.getInt(1) > 0;
    } catch (SQLException e) {
      System.out.println("중복 예약 체크 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return isDuplicate;
  }

  // ─────────────────────────────────────────────────────
  // 5. 예약 저장 (INSERT)
  // ─────────────────────────────────────────────────────
  public int saveReservation(Reservation reservation) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO reservation " +
        "(period_id, user_id, facility_id, equipment_id, purpose, " +
        " created_at, reservation_date, status, real_use, target_type) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, '대기', 'false', ?)";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, reservation.getPeriod().getPeriodId());
      pstmt.setInt(2, reservation.getUser().getUserId());
      if (reservation.getFacility() != null) pstmt.setLong(3, reservation.getFacility().getFacilityId());
      else pstmt.setNull(3, Types.BIGINT);
      if (reservation.getEquipment() != null) pstmt.setLong(4, reservation.getEquipment().getEquipmentId());
      else pstmt.setNull(4, Types.BIGINT);
      pstmt.setString(5, reservation.getPurpose());
      pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.setDate(7, Date.valueOf(reservation.getReservationDate()));
      pstmt.setString(8, reservation.getTargetType());
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("예약 저장 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 6. 내 예약 목록 조회 (사용자용)
  // ─────────────────────────────────────────────────────
  public List<Reservation> findReservationsByUserId(int userId) {
    return findReservationsByCondition("WHERE r.user_id = ?", userId, null);
  }

  // ─────────────────────────────────────────────────────
  // 7. 반납 가능한 예약 목록 조회 (status = '승인')
  // ─────────────────────────────────────────────────────
  public List<Reservation> findReturnableReservations(int userId) {
    return findReservationsByCondition("WHERE r.user_id = ? AND r.status = '승인'", userId, null);
  }

  // ─────────────────────────────────────────────────────
  // 8. 대기 중인 예약 목록 조회 (관리자용 - 승인/반려 처리)
  //    → status = '대기' 인 것만
  // ─────────────────────────────────────────────────────
  public List<Reservation> findPendingReservations() {
    return findReservationsByCondition("WHERE r.status = '대기'", null, null);
  }

  // ─────────────────────────────────────────────────────
  // 9. 승인된 예약 목록 조회 (관리자용 - 강제 취소)
  //    → status = '승인' 인 것만
  // ─────────────────────────────────────────────────────
  public List<Reservation> findApprovedReservations() {
    return findReservationsByCondition("WHERE r.status = '승인'", null, null);
  }

  // ─────────────────────────────────────────────────────
  // 공통 예약 목록 조회 메서드
  // 위 6~9번 메서드들이 모두 이걸 호출해요
  // WHERE 조건만 바꿔서 재사용
  // ─────────────────────────────────────────────────────
  private List<Reservation> findReservationsByCondition(String whereClause,
                                                        Integer userId,
                                                        String status) {
    List<Reservation> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT r.reservation_id, r.reservation_date, r.status, " +
        "       r.purpose, r.target_type, r.created_at, " +
        "       u.name AS user_name, " +
        "       p.period_id, p.period_name, p.start_time, p.end_time, " +
        "       f.name AS facility_name, " +
        "       e.name AS equipment_name " +
        "FROM reservation r " +
        "JOIN period p ON r.period_id = p.period_id " +
        "JOIN user u ON r.user_id = u.user_id " +
        "LEFT JOIN facility f ON r.facility_id = f.facility_id " +
        "LEFT JOIN equipment e ON r.equipment_id = e.equipment_id " +
        whereClause + " " +
        "ORDER BY r.reservation_date DESC, r.created_at DESC";

    try {
      pstmt = conn.prepareStatement(sql);
      // userId 있으면 첫번째 ? 에 세팅
      if (userId != null) pstmt.setInt(1, userId);

      rs = pstmt.executeQuery();

      while (rs.next()) {
        Period period = Period.builder()
            .periodId(rs.getInt("period_id"))
            .periodName(rs.getString("period_name"))
            .startTime(rs.getTime("start_time").toLocalTime())
            .endTime(rs.getTime("end_time").toLocalTime())
            .build();

        Facility facility = null;
        if (rs.getString("facility_name") != null) {
          facility = Facility.builder()
              .name(rs.getString("facility_name")).build();
        }

        Equipment equipment = null;
        if (rs.getString("equipment_name") != null) {
          equipment = Equipment.builder()
              .name(rs.getString("equipment_name")).build();
        }

        // 예약자 이름도 담기 (관리자 화면에서 보여주기 위해)
        User user = User.builder()
            .userId(userId != null ? userId : 0)
            .name(rs.getString("user_name"))
            .build();

        list.add(Reservation.builder()
            .reservationId(rs.getLong("reservation_id"))
            .reservationDate(rs.getDate("reservation_date").toLocalDate())
            .status(rs.getString("status"))
            .purpose(rs.getString("purpose"))
            .targetType(rs.getString("target_type"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .period(period)
            .facility(facility)
            .equipment(equipment)
            .user(user)
            .build());
      }
    } catch (SQLException e) {
      System.out.println("예약 목록 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 10. 예약 승인
  //     → status = '승인', approved_at = 현재시간
  // ─────────────────────────────────────────────────────
  public int approveReservation(long reservationId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE reservation SET status = '승인', approved_at = ? " +
        "WHERE reservation_id = ? AND status = '대기'";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.setLong(2, reservationId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("예약 승인 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 11. 예약 반려
  //     → status = '거절'
  //     → 반려 사유는 purpose 컬럼에 추가로 기록
  // ─────────────────────────────────────────────────────
  public int rejectReservation(long reservationId, String reason) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    // 기존 purpose 뒤에 반려사유 붙이기
    String sql = "UPDATE reservation " +
        "SET status = '거절', " +
        "    purpose = CONCAT(purpose, ' [반려사유: ', ?, ']') " +
        "WHERE reservation_id = ? AND status = '대기'";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, reason);
      pstmt.setLong(2, reservationId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("예약 반려 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 12. 예약 강제 취소 (관리자용)
  //     → status = '취소'
  //     → 강제 취소 사유도 purpose에 기록
  // ─────────────────────────────────────────────────────
  public int forcecancelReservation(long reservationId, String reason) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE reservation " +
        "SET status = '취소', " +
        "    purpose = CONCAT(purpose, ' [강제취소사유: ', ?, ']') " +
        "WHERE reservation_id = ? AND status = '승인'";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, reason);
      pstmt.setLong(2, reservationId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("예약 강제 취소 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 13. 반납 처리
  // ─────────────────────────────────────────────────────
  public int saveReturnRequest(long reservationId, String condition) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String insertSql = "INSERT INTO return_request (reservation_id, condition, status, created_at) " +
        "VALUES (?, ?, '반납완료', ?)";
    String updateSql = "UPDATE reservation SET status = '반납완료' WHERE reservation_id = ?";

    try {
      pstmt = conn.prepareStatement(insertSql);
      pstmt.setLong(1, reservationId);
      pstmt.setString(2, condition);
      pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.executeUpdate();
      db.close(pstmt);

      pstmt = conn.prepareStatement(updateSql);
      pstmt.setLong(1, reservationId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("반납 처리 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 14. 예약 취소 (사용자용)
  // ─────────────────────────────────────────────────────
  public int cancelReservation(long reservationId, int userId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE reservation SET status = '취소' " +
        "WHERE reservation_id = ? AND user_id = ? " +
        "AND status IN ('대기', '승인')";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setLong(1, reservationId);
      pstmt.setInt(2, userId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("예약 취소 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }
}