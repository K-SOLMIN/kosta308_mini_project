package com.kimdoolim.dao;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FacilityEquipmentDAO {

  private final Database db = MySql.getMySql();

  // ─────────────────────────────────────────────────────
  // 1. 전체 시설 목록 조회 (상위관리자용 - 담당자 없는 것 포함 전체)
  // ─────────────────────────────────────────────────────
  public List<Facility> findAllFacilities() {
    List<Facility> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT f.facility_id, f.manager_id, f.location, f.name, f.max_capacity, " +
        "       f.max_reservation_unit, f.max_reservation_value, " +
        "       f.is_delete, f.status, u.name AS manager_name " +
        "FROM facility f " +
        "LEFT JOIN user u ON f.manager_id = u.user_id " +
        "WHERE f.is_delete = 'false' " +
        "ORDER BY f.facility_id";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        User manager = null;
        if (rs.getString("manager_name") != null) {
          manager = User.builder()
              .name(rs.getString("manager_name"))
              .build();
        }

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
  // 2. 전체 비품 목록 조회 (상위관리자용 - 담당자 없는 것 포함 전체)
  // ─────────────────────────────────────────────────────
  public List<Equipment> findAllEquipments() {
    List<Equipment> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT e.equipment_id, e.manager_id, e.name, e.location, e.serial_no, " +
        "       e.status, u.name AS manager_name " +
        "FROM equipment e " +
        "LEFT JOIN user u ON e.manager_id = u.user_id " +
        "WHERE e.check_delete = 'false' " +
        "ORDER BY e.equipment_id";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();

      while (rs.next()) {
        User manager = null;
        if (rs.getString("manager_name") != null) {
          manager = User.builder()
              .name(rs.getString("manager_name"))
              .build();
        }

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
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
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
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
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
    ResultSet rs = null;
    int result = 0;

    String lockSql   = "SELECT facility_id FROM facility WHERE facility_id = ? FOR UPDATE";
    String updateSql = "UPDATE facility SET status = ? WHERE facility_id = ?";

    try {
      pstmt = conn.prepareStatement(lockSql);
      pstmt.setLong(1, facilityId);
      rs = pstmt.executeQuery();

      if (!rs.next()) {
        System.out.println("시설을 찾을 수 없습니다. (facility_id=" + facilityId + ")");
        db.rollback(conn);
        return 0;
      }
      db.close(rs); db.close(pstmt);

      pstmt = conn.prepareStatement(updateSql);
      pstmt.setString(1, status);
      pstmt.setLong(2, facilityId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      if (e.getErrorCode() == 1205) {
        System.out.println("시설 상태 수정 실패: 다른 관리자가 수정 중입니다. 잠시 후 다시 시도해주세요.");
      } else {
        System.out.println("시설 상태 수정 실패: " + e.getMessage());
      }
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 6. 비품 상태 수정
  // ─────────────────────────────────────────────────────
  public int updateEquipmentStatus(long equipmentId, String status) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int result = 0;

    String lockSql   = "SELECT equipment_id FROM equipment WHERE equipment_id = ? FOR UPDATE";
    String updateSql = "UPDATE equipment SET status = ? WHERE equipment_id = ?";

    try {
      pstmt = conn.prepareStatement(lockSql);
      pstmt.setLong(1, equipmentId);
      rs = pstmt.executeQuery();

      if (!rs.next()) {
        System.out.println("비품을 찾을 수 없습니다. (equipment_id=" + equipmentId + ")");
        db.rollback(conn);
        return 0;
      }
      db.close(rs); db.close(pstmt);

      pstmt = conn.prepareStatement(updateSql);
      pstmt.setString(1, status);
      pstmt.setLong(2, equipmentId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      if (e.getErrorCode() == 1205) {
        System.out.println("비품 상태 수정 실패: 다른 관리자가 수정 중입니다. 잠시 후 다시 시도해주세요.");
      } else {
        System.out.println("비품 상태 수정 실패: " + e.getMessage());
      }
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 7. 시설 삭제 (is_delete = 'true' 로 변경)
  // ─────────────────────────────────────────────────────
  public int deleteFacility(long facilityId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int result = 0;

    String lockSql   = "SELECT facility_id FROM facility WHERE facility_id = ? FOR UPDATE";
    String updateSql = "UPDATE facility SET is_delete = 'true', deletedate = ? WHERE facility_id = ?";

    try {
      pstmt = conn.prepareStatement(lockSql);
      pstmt.setLong(1, facilityId);
      rs = pstmt.executeQuery();

      if (!rs.next()) {
        System.out.println("시설을 찾을 수 없습니다. (facility_id=" + facilityId + ")");
        db.rollback(conn);
        return 0;
      }
      db.close(rs); db.close(pstmt);

      pstmt = conn.prepareStatement(updateSql);
      pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.setLong(2, facilityId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      if (e.getErrorCode() == 1205) {
        System.out.println("시설 삭제 실패: 다른 관리자가 수정 중입니다. 잠시 후 다시 시도해주세요.");
      } else {
        System.out.println("시설 삭제 실패: " + e.getMessage());
      }
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 8. 비품 삭제 (check_delete = 'true' 로 변경)
  // ─────────────────────────────────────────────────────
  public int deleteEquipment(long equipmentId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int result = 0;

    String lockSql   = "SELECT equipment_id FROM equipment WHERE equipment_id = ? FOR UPDATE";
    String updateSql = "UPDATE equipment SET check_delete = 'true', deletedate = ? WHERE equipment_id = ?";

    try {
      pstmt = conn.prepareStatement(lockSql);
      pstmt.setLong(1, equipmentId);
      rs = pstmt.executeQuery();

      if (!rs.next()) {
        System.out.println("비품을 찾을 수 없습니다. (equipment_id=" + equipmentId + ")");
        db.rollback(conn);
        return 0;
      }
      db.close(rs); db.close(pstmt);

      pstmt = conn.prepareStatement(updateSql);
      pstmt.setDate(1, Date.valueOf(java.time.LocalDate.now()));
      pstmt.setLong(2, equipmentId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      if (e.getErrorCode() == 1205) {
        System.out.println("비품 삭제 실패: 다른 관리자가 수정 중입니다. 잠시 후 다시 시도해주세요.");
      } else {
        System.out.println("비품 삭제 실패: " + e.getMessage());
      }
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 9. 중간관리자 담당 시설 목록 조회
  // ─────────────────────────────────────────────────────
  public List<Facility> findFacilitiesByManagerId(int managerId) {
    List<Facility> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT f.facility_id, f.location, f.name, f.max_capacity, " +
        "       f.max_reservation_unit, f.max_reservation_value, f.status " +
        "FROM facility f " +
        "WHERE f.is_delete = 'false' AND f.manager_id = ? " +
        "ORDER BY f.facility_id";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, managerId);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(Facility.builder()
            .facilityId(rs.getLong("facility_id"))
            .location(rs.getString("location"))
            .name(rs.getString("name"))
            .maxCapacity(rs.getInt("max_capacity"))
            .maxReservationUnit(rs.getString("max_reservation_unit"))
            .maxReservationValue(rs.getInt("max_reservation_value"))
            .status(rs.getString("status"))
            .build());
      }
    } catch (SQLException e) {
      System.out.println("담당 시설 목록 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 10. 중간관리자 담당 비품 목록 조회
  // ─────────────────────────────────────────────────────
  public List<Equipment> findEquipmentsByManagerId(int managerId) {
    List<Equipment> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT e.equipment_id, e.name, e.location, e.serial_no, e.status " +
        "FROM equipment e " +
        "WHERE e.check_delete = 'false' AND e.manager_id = ? " +
        "ORDER BY e.equipment_id";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, managerId);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(Equipment.builder()
            .equipmentId(rs.getLong("equipment_id"))
            .name(rs.getString("name"))
            .location(rs.getString("location"))
            .serialNo(rs.getString("serial_no"))
            .status(rs.getString("status"))
            .build());
      }
    } catch (SQLException e) {
      System.out.println("담당 비품 목록 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 11. 시설 담당자 변경 (재배정 / 담당자 없음 처리)
  //     managerId = null → 담당자 없음
  //     managerId = 값   → 해당 관리자로 재배정
  // ─────────────────────────────────────────────────────
  public int updateFacilityManager(long facilityId, Integer managerId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE facility SET manager_id = ? WHERE facility_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      if (managerId != null) pstmt.setInt(1, managerId);
      else pstmt.setNull(1, Types.INTEGER);
      pstmt.setLong(2, facilityId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("시설 담당자 변경 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 12. 비품 담당자 변경 (재배정 / 담당자 없음 처리)
  // ─────────────────────────────────────────────────────
  public int updateEquipmentManager(long equipmentId, Integer managerId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE equipment SET manager_id = ? WHERE equipment_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      if (managerId != null) pstmt.setInt(1, managerId);
      else pstmt.setNull(1, Types.INTEGER);
      pstmt.setLong(2, equipmentId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("비품 담당자 변경 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 13. 특정 관리자의 모든 시설 담당자 NULL 처리
  //     (휴직/전근 시 호출)
  // ─────────────────────────────────────────────────────
  public int clearManagerFromFacilities(int managerId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    try {
      pstmt = conn.prepareStatement("UPDATE facility SET manager_id = NULL WHERE manager_id = ?");
      pstmt.setInt(1, managerId);
      int result = pstmt.executeUpdate();
      db.commit(conn);
      return result;
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("시설 담당자 초기화 실패: " + e.getMessage());
      return 0;
    } finally {
      db.close(pstmt); db.close(conn);
    }
  }

  // ─────────────────────────────────────────────────────
  // 14. 특정 관리자의 모든 비품 담당자 NULL 처리
  //     (휴직/전근 시 호출)
  // ─────────────────────────────────────────────────────
  public int clearManagerFromEquipments(int managerId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    try {
      pstmt = conn.prepareStatement("UPDATE equipment SET manager_id = NULL WHERE manager_id = ?");
      pstmt.setInt(1, managerId);
      int result = pstmt.executeUpdate();
      db.commit(conn);
      return result;
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("비품 담당자 초기화 실패: " + e.getMessage());
      return 0;
    } finally {
      db.close(pstmt); db.close(conn);
    }
  }
}