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
        "bs.repeat_start_date, bs.repeat_end_date, bs.description, " +
        "bs.facility_id, bs.equipment_id, p.period_name " +
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
        map.put("facilityId",  rs.getObject("facility_id"));
        map.put("equipmentId", rs.getObject("equipment_id"));
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
  //    facilityId/equipmentId = null → 전체 적용 (상위관리자)
  //    facilityId/equipmentId = 값   → 해당 시설/비품만 (중간관리자)
  // ─────────────────────────────────────────────────────
  public int saveSpecificBlock(LocalDate date, int periodId, String description,
                               Long facilityId, Long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO block_schedule (block_date, period_id, description, facility_id, equipment_id) " +
        "VALUES (?, ?, ?, ?, ?)";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setDate(1, Date.valueOf(date));
      pstmt.setInt(2, periodId);
      pstmt.setString(3, description);
      if (facilityId != null) pstmt.setLong(4, facilityId);
      else pstmt.setNull(4, Types.BIGINT);
      if (equipmentId != null) pstmt.setLong(5, equipmentId);
      else pstmt.setNull(5, Types.BIGINT);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("등록 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 3. 반복 요일 + 교시 차단 등록
  // ─────────────────────────────────────────────────────
  public int saveRepeatBlock(int dayOfWeek, LocalDate startDate, LocalDate endDate,
                             int periodId, String description,
                             Long facilityId, Long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO block_schedule " +
        "(repeat_day_of_week, repeat_start_date, repeat_end_date, period_id, description, facility_id, equipment_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, dayOfWeek);
      pstmt.setDate(2, Date.valueOf(startDate));
      pstmt.setDate(3, Date.valueOf(endDate));
      pstmt.setInt(4, periodId);
      pstmt.setString(5, description);
      if (facilityId != null) pstmt.setLong(6, facilityId);
      else pstmt.setNull(6, Types.BIGINT);
      if (equipmentId != null) pstmt.setLong(7, equipmentId);
      else pstmt.setNull(7, Types.BIGINT);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("등록 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 4. 삭제
  // ─────────────────────────────────────────────────────
  public int delete(long blockScheduleId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "DELETE FROM block_schedule WHERE block_schedule_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
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
  // 5. 예약 가능 여부 체크
  //    - facility_id IS NULL → 전체 시설/비품에 적용
  //    - facility_id = 값    → 해당 시설만 적용
  // ─────────────────────────────────────────────────────
  public String findBlockedReason(LocalDate date, int periodId, Long facilityId, Long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String reason = null;

    String sql = "SELECT description FROM block_schedule " +
        "WHERE period_id = ? " +
        "AND (" +
        "    (block_date = ?) " +
        "    OR " +
        "    (repeat_day_of_week = DAYOFWEEK(?) " +
        "     AND ? BETWEEN repeat_start_date AND repeat_end_date)" +
        ") " +
        "AND (facility_id IS NULL OR facility_id = ?) " +
        "AND (equipment_id IS NULL OR equipment_id = ?) " +
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
}