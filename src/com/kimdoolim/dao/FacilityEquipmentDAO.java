package com.kimdoolim.dao;

import com.kimdoolim.common.DataBase;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FacilityEquipmentDAO {

  private final DataBase db = MySql.getMySql();

  // ─────────────────────────────────────────────────────
  // 1. 전체 시설 목록 조회
  // ─────────────────────────────────────────────────────
  public List<Facility> findAllFacilities() {
    List<Facility> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT f.facility_id, f.location, f.name, f.max_capacity, " +
        "       f.max_reservation_unit, f.max_reservation_value, " +
        "       f.is_delete, f.status, u.name AS manager_name " +
        "FROM facility f " +
        "JOIN user u ON f.manager_id = u.user_id " +
        "WHERE f.is_delete = 'false' " +
        "ORDER BY f.facility_id";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        User manager = User.builder()
            .name(rs.getString("manager_name"))
            .build();

        list.add(Facility.builder()
            .facilityId(rs.getLong("facility_id"))
            .location(rs.getString("location"))
            .name(rs.getString("name"))
            .maxCapacity(rs.getInt("max_capacity"))
            .maxReservationUnit(rs.getString("max_reservation_unit"))
            .maxReservationValue(rs.getInt("max_reservation_value"))
            .status(rs.getString("status"))
            .user(manager)
            .build());
      }
    } catch (SQLException e) {
      System.out.println("시설 목록 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 2. 전체 비품 목록 조회
  // ─────────────────────────────────────────────────────
  public List<Equipment> findAllEquipments() {
    List<Equipment> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT e.equipment_id, e.name, e.location, e.serial_no, " +
        "       e.status, u.name AS manager_name " +
        "FROM equipment e " +
        "JOIN user u ON e.manager_id = u.user_id " +
        "WHERE e.check_delete = 'false' " +
        "ORDER BY e.equipment_id";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        User manager = User.builder()
            .name(rs.getString("manager_name"))
            .build();

        list.add(Equipment.builder()
            .equipmentId(rs.getLong("equipment_id"))
            .name(rs.getString("name"))
            .location(rs.getString("location"))
            .serialNo(rs.getString("serial_no"))
            .status(rs.getString("status"))
            .user(manager)
            .build());
      }
    } catch (SQLException e) {
      System.out.println("비품 목록 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 3. 시설 등록
  // ─────────────────────────────────────────────────────
  public int saveFacility(Facility facility, int managerId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO facility " +
        "(manager_id, location, name, max_capacity, " +
        " max_reservation_unit, max_reservation_value, is_delete, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, 'false', ?)";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, managerId);
      pstmt.setString(2, facility.getLocation());
      pstmt.setString(3, facility.getName());
      pstmt.setInt(4, facility.getMaxCapacity());
      pstmt.setString(5, facility.getMaxReservationUnit());
      pstmt.setInt(6, facility.getMaxReservationValue());
      pstmt.setString(7, facility.getStatus());
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("시설 등록 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 4. 비품 등록
  // ─────────────────────────────────────────────────────
  public int saveEquipment(Equipment equipment, int managerId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO equipment " +
        "(manager_id, name, location, check_delete, serial_no, status) " +
        "VALUES (?, ?, ?, 'false', ?, ?)";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, managerId);
      pstmt.setString(2, equipment.getName());
      pstmt.setString(3, equipment.getLocation());
      pstmt.setString(4, equipment.getSerialNo());
      pstmt.setString(5, equipment.getStatus());
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("비품 등록 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 5. 시설 상태 수정
  // ─────────────────────────────────────────────────────
  public int updateFacilityStatus(long facilityId, String status) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE facility SET status = ? WHERE facility_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, status);
      pstmt.setLong(2, facilityId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("시설 상태 수정 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 6. 비품 상태 수정
  // ─────────────────────────────────────────────────────
  public int updateEquipmentStatus(long equipmentId, String status) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE equipment SET status = ? WHERE equipment_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, status);
      pstmt.setLong(2, equipmentId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("비품 상태 수정 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 7. 시설 삭제 (실제 삭제 아닌 is_delete = 'true' 로 변경)
  // ─────────────────────────────────────────────────────
  public int deleteFacility(long facilityId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE facility SET is_delete = 'true', deletedate = ? " +
        "WHERE facility_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.setLong(2, facilityId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("시설 삭제 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 8. 비품 삭제 (실제 삭제 아닌 check_delete = 'true' 로 변경)
  // ─────────────────────────────────────────────────────
  public int deleteEquipment(long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE equipment SET check_delete = 'true', deletedate = ? " +
        "WHERE equipment_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setDate(1, Date.valueOf(java.time.LocalDate.now()));
      pstmt.setLong(2, equipmentId);
      result = pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("비품 삭제 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }
}