package com.kimdoolim.dao;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockScheduleDAO {

  private final Database db = MySql.getMySql();

  // ─────────────────────────────────────────────────────
  // 1. 전체 목록 조회
  // ─────────────────────────────────────────────────────
  public List<Map<String, Object>> findAll() {
    List<Map<String, Object>> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT bs.block_schedule_id, bs.block_date, bs.repeat_day_of_week, " +
        "bs.repeat_start_date, bs.repeat_end_date, bs.description, p.period_name " +
        "FROM block_schedule bs " +
        "JOIN period p ON bs.period_id = p.period_id " +
        "ORDER BY bs.block_schedule_id DESC";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",          rs.getLong("block_schedule_id"));
        map.put("date",        rs.getDate("block_date") != null ? rs.getDate("block_date").toLocalDate() : null);
        map.put("repeatDay",   rs.getObject("repeat_day_of_week"));
        map.put("repeatStart", rs.getDate("repeat_start_date") != null ? rs.getDate("repeat_start_date").toLocalDate() : null);
        map.put("repeatEnd",   rs.getDate("repeat_end_date") != null ? rs.getDate("repeat_end_date").toLocalDate() : null);
        map.put("desc",        rs.getString("description"));
        map.put("periodName",  rs.getString("period_name"));
        list.add(map);
      }
    } catch (SQLException e) {
      System.out.println("조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 2. 특정 날짜 + 교시 차단 등록
  // ─────────────────────────────────────────────────────
  public long saveSpecificBlock(LocalDate date, int periodId, String description) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    long generatedId = -1;

    String sql = "INSERT INTO block_schedule (block_date, period_id, description) VALUES (?, ?, ?)";

    try {
      pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      pstmt.setDate(1, Date.valueOf(date));
      pstmt.setInt(2, periodId);
      pstmt.setString(3, description);
      pstmt.executeUpdate();
      rs = pstmt.getGeneratedKeys();
      if (rs.next()) generatedId = rs.getLong(1);
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("등록 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return generatedId;
  }

  // ─────────────────────────────────────────────────────
  // 3. 반복 요일 + 교시 차단 등록
  // ─────────────────────────────────────────────────────
  public long saveRepeatBlock(int dayOfWeek, LocalDate startDate, LocalDate endDate,
                              int periodId, String description) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    long generatedId = -1;

    String sql = "INSERT INTO block_schedule " +
        "(repeat_day_of_week, repeat_start_date, repeat_end_date, period_id, description) " +
        "VALUES (?, ?, ?, ?, ?)";

    try {
      pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      pstmt.setInt(1, dayOfWeek);
      pstmt.setDate(2, Date.valueOf(startDate));
      pstmt.setDate(3, Date.valueOf(endDate));
      pstmt.setInt(4, periodId);
      pstmt.setString(5, description);
      pstmt.executeUpdate();
      rs = pstmt.getGeneratedKeys();
      if (rs.next()) generatedId = rs.getLong(1);
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("등록 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return generatedId;
  }

  // ─────────────────────────────────────────────────────
  // 4. 삭제 (detail 먼저 삭제 후 master 삭제)
  // ─────────────────────────────────────────────────────
  public int delete(long blockScheduleId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    try {
      pstmt = conn.prepareStatement(
          "DELETE FROM block_schedule_detail WHERE block_schedule_id = ?");
      pstmt.setLong(1, blockScheduleId);
      pstmt.executeUpdate();
      db.close(pstmt);

      pstmt = conn.prepareStatement(
          "DELETE FROM block_schedule WHERE block_schedule_id = ?");
      pstmt.setLong(1, blockScheduleId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("삭제 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 5. 예약 가능 여부 체크 (block_schedule_detail JOIN)
  // ─────────────────────────────────────────────────────
  public String findBlockedReason(LocalDate date, int periodId, Long facilityId, Long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String reason = null;

    String sql = "SELECT bs.description " +
        "FROM block_schedule bs " +
        "JOIN block_schedule_detail bsd ON bs.block_schedule_id = bsd.block_schedule_id " +
        "WHERE bs.period_id = ? " +
        "AND (" +
        "    (bs.block_date = ?) " +
        "    OR " +
        "    (bs.repeat_day_of_week = DAYOFWEEK(?) " +
        "     AND ? BETWEEN bs.repeat_start_date AND bs.repeat_end_date)" +
        ") " +
        "AND (bsd.facility_id = ? OR bsd.equipment_id = ?) " +
        "LIMIT 1";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, periodId);
      pstmt.setDate(2, Date.valueOf(date));
      pstmt.setDate(3, Date.valueOf(date));
      pstmt.setDate(4, Date.valueOf(date));
      if (facilityId != null) pstmt.setLong(5, facilityId);
      else pstmt.setNull(5, Types.BIGINT);
      if (equipmentId != null) pstmt.setLong(6, equipmentId);
      else pstmt.setNull(6, Types.BIGINT);
      rs = pstmt.executeQuery();
      if (rs.next()) reason = rs.getString("description");
    } catch (SQLException e) {
      System.out.println("차단 체크 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return reason;
  }

  // ─────────────────────────────────────────────────────
  // 6. 적용 대상 추가 (단건)
  // ─────────────────────────────────────────────────────
  public int saveBlockDetail(long blockScheduleId, Long facilityId, Long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO block_schedule_detail (block_schedule_id, facility_id, equipment_id) " +
        "VALUES (?, ?, ?)";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setLong(1, blockScheduleId);
      if (facilityId != null) pstmt.setLong(2, facilityId);
      else pstmt.setNull(2, Types.BIGINT);
      if (equipmentId != null) pstmt.setLong(3, equipmentId);
      else pstmt.setNull(3, Types.BIGINT);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("적용 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 7. 전체 시설/비품 일괄 적용
  // ─────────────────────────────────────────────────────
  public int applyBlockDetailToAll(long blockScheduleId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int count = 0;

    try {
      pstmt = conn.prepareStatement(
          "INSERT INTO block_schedule_detail (block_schedule_id, facility_id) " +
          "SELECT ?, facility_id FROM facility WHERE is_delete = 'false'");
      pstmt.setLong(1, blockScheduleId);
      count += pstmt.executeUpdate();
      db.close(pstmt);

      pstmt = conn.prepareStatement(
          "INSERT INTO block_schedule_detail (block_schedule_id, equipment_id) " +
          "SELECT ?, equipment_id FROM equipment WHERE check_delete = 'false'");
      pstmt.setLong(1, blockScheduleId);
      count += pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("전체 적용 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return count;
  }

  // ─────────────────────────────────────────────────────
  // 8. 단건 조회 (취소 로직에서 블록 정보 가져올 때 사용)
  // ─────────────────────────────────────────────────────
  public Map<String, Object> getById(long blockScheduleId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Map<String, Object> map = null;

    String sql = "SELECT block_schedule_id, block_date, repeat_day_of_week, " +
        "repeat_start_date, repeat_end_date, period_id, description " +
        "FROM block_schedule WHERE block_schedule_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setLong(1, blockScheduleId);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        map = new HashMap<>();
        map.put("id",          rs.getLong("block_schedule_id"));
        map.put("date",        rs.getDate("block_date") != null ? rs.getDate("block_date").toLocalDate() : null);
        map.put("repeatDay",   rs.getObject("repeat_day_of_week"));
        map.put("repeatStart", rs.getDate("repeat_start_date") != null ? rs.getDate("repeat_start_date").toLocalDate() : null);
        map.put("repeatEnd",   rs.getDate("repeat_end_date") != null ? rs.getDate("repeat_end_date").toLocalDate() : null);
        map.put("periodId",    rs.getInt("period_id"));
        map.put("desc",        rs.getString("description"));
      }
    } catch (SQLException e) {
      System.out.println("단건 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return map;
  }

  // ─────────────────────────────────────────────────────
  // 9. 적용된 시설/비품 목록 조회 (표시용)
  //    isAll: 전체 시설+비품 수와 일치하면 true
  // ─────────────────────────────────────────────────────
  public Map<String, Object> getBlockDetailsForDisplay(long blockScheduleId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    List<String> facilities = new ArrayList<>();
    List<String> equipments = new ArrayList<>();
    Map<String, Object> result = new HashMap<>();

    try {
      String sql = "SELECT f.name AS facility_name, e.name AS equipment_name " +
          "FROM block_schedule_detail bsd " +
          "LEFT JOIN facility f ON bsd.facility_id = f.facility_id " +
          "LEFT JOIN equipment e ON bsd.equipment_id = e.equipment_id " +
          "WHERE bsd.block_schedule_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setLong(1, blockScheduleId);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        String fn = rs.getString("facility_name");
        String en = rs.getString("equipment_name");
        if (fn != null) facilities.add(fn);
        if (en != null) equipments.add(en);
      }
      db.close(rs); rs = null;
      db.close(pstmt); pstmt = null;

      pstmt = conn.prepareStatement(
          "SELECT (SELECT COUNT(*) FROM facility WHERE is_delete = 'false') AS tf, " +
          "(SELECT COUNT(*) FROM equipment WHERE check_delete = 'false') AS te");
      rs = pstmt.executeQuery();
      boolean isAll = false;
      if (rs.next()) {
        int totalF = rs.getInt("tf");
        int totalE = rs.getInt("te");
        isAll = totalF + totalE > 0
            && facilities.size() == totalF
            && equipments.size() == totalE;
      }

      result.put("isAll", isAll);
      result.put("facilities", facilities);
      result.put("equipments", equipments);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return result;
  }
}