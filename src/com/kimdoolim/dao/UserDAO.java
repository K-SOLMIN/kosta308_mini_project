package com.kimdoolim.dao;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

  private final Database db = MySql.getMySql();

  // ─────────────────────────────────────────────────────
  // 1. 전체 사용자 목록 조회 (관리자 제외)
  // ─────────────────────────────────────────────────────
  public List<User> findAllUsers() {
    List<User> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT user_id, school_id, id, permission, name, phone, " +
        "grade_no, class_no, is_active, user_status " +
        "FROM user WHERE permission != 'ADMIN' " +
        "ORDER BY permission, name";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(User.builder()
            .userId(rs.getInt("user_id"))
            .schoolId(rs.getInt("school_id"))
            .id(rs.getString("id"))
            .permission(Permission.valueOf(rs.getString("permission")))
            .name(rs.getString("name"))
            .phone(rs.getString("phone"))
            .gradeNo(rs.getInt("grade_no"))
            .classNo(rs.getInt("class_no"))
            .isActive(Boolean.parseBoolean(rs.getString("is_active")))
            .userStatus(rs.getString("user_status"))
            .build());
      }
    } catch (SQLException e) {
      System.out.println("사용자 목록 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 2. 중간관리자 목록 조회 (활성 상태만)
  // ─────────────────────────────────────────────────────
  public List<User> findMiddleAdmins() {
    List<User> list = new ArrayList<>();
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    String sql = "SELECT user_id, name, id FROM user " +
        "WHERE permission = 'MIDDLEADMIN' AND is_active = 'true'";

    try {
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        list.add(User.builder()
            .userId(rs.getInt("user_id"))
            .name(rs.getString("name"))
            .id(rs.getString("id"))
            .build());
      }
    } catch (SQLException e) {
      System.out.println("중간관리자 목록 조회 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return list;
  }

  // ─────────────────────────────────────────────────────
  // 3. 사용자 등록
  // ─────────────────────────────────────────────────────
  public int saveUser(User user) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO user (school_id, id, password, permission, name, " +
        "phone, grade_no, class_no, is_active, user_status) " +
        "VALUES (?, ?, ?, 'USER', ?, ?, ?, ?, 'true', 'ACTIVE')";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, user.getSchoolId());
      pstmt.setString(2, user.getId());
      pstmt.setString(3, user.getPassword());
      pstmt.setString(4, user.getName());
      pstmt.setString(5, user.getPhone());
      pstmt.setInt(6, user.getGradeNo());
      pstmt.setInt(7, user.getClassNo());
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("사용자 등록 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 4. 휴직 처리 (is_active=false, user_status='휴직')
  // ─────────────────────────────────────────────────────
  public int setLeaveOfAbsence(int userId) {
    return updateUserStatus(userId, false, "휴직");
  }

  // ─────────────────────────────────────────────────────
  // 5. 전근 처리 (is_active=false, user_status='전근')
  // ─────────────────────────────────────────────────────
  public int setTransfer(int userId) {
    return updateUserStatus(userId, false, "전근");
  }

  // ─────────────────────────────────────────────────────
  // 6. 복직 처리 (is_active=true, user_status='ACTIVE')
  // ─────────────────────────────────────────────────────
  public int restoreFromLeave(int userId) {
    return updateUserStatus(userId, true, "ACTIVE");
  }

  // ─────────────────────────────────────────────────────
  // 7. 전근 승인 (새 학교 관리자가 승인 → 활성화)
  // ─────────────────────────────────────────────────────
  public int approveTransfer(int userId) {
    return updateUserStatus(userId, true, "ACTIVE");
  }

  // ─────────────────────────────────────────────────────
  // 공통 상태 업데이트
  // ─────────────────────────────────────────────────────
  private int updateUserStatus(int userId, boolean isActive, String userStatus) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE user SET is_active = ?, user_status = ? WHERE user_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, String.valueOf(isActive));
      pstmt.setString(2, userStatus);
      pstmt.setInt(3, userId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("사용자 상태 변경 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 8. 권한 변경 (USER ↔ MIDDLEADMIN)
  // ─────────────────────────────────────────────────────
  public int updatePermission(int userId, Permission permission) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE user SET permission = ? WHERE user_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, permission.name());
      pstmt.setInt(2, userId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("권한 변경 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 9. 비밀번호 변경
  // ─────────────────────────────────────────────────────
  public int updatePassword(int userId, String newPassword) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE user SET password = ? WHERE user_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, newPassword);
      pstmt.setInt(2, userId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("비밀번호 변경 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 10. 아이디 중복 체크
  // ─────────────────────────────────────────────────────
  public boolean isIdDuplicate(String id) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    boolean isDuplicate = false;

    String sql = "SELECT COUNT(*) FROM user WHERE id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, id);
      rs = pstmt.executeQuery();
      if (rs.next()) isDuplicate = rs.getInt(1) > 0;
    } catch (SQLException e) {
      System.out.println("아이디 중복 체크 실패: " + e.getMessage());
    } finally {
      db.close(rs); db.close(pstmt); db.close(conn);
    }
    return isDuplicate;
  }
}