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
        "grade_no, class_no, is_active " +
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
  // 2. 사용자 등록
  // ─────────────────────────────────────────────────────
  public int saveUser(User user) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "INSERT INTO user (school_id, id, password, permission, name, " +
        "phone, grade_no, class_no, is_active) " +
        "VALUES (?, ?, ?, 'USER', ?, ?, ?, ?, 'true')";

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
  // 3. 사용자 비활성화 (휴직/전근)
  // ─────────────────────────────────────────────────────
  public int deactivateUser(int userId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE user SET is_active = 'false' WHERE user_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, userId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("사용자 비활성화 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 4. 사용자 활성화 (복직)
  // ─────────────────────────────────────────────────────
  public int activateUser(int userId) {
    Connection conn = db.getConnection();
    PreparedStatement pstmt = null;
    int result = 0;

    String sql = "UPDATE user SET is_active = 'true' WHERE user_id = ?";

    try {
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, userId);
      result = pstmt.executeUpdate();
      db.commit(conn);
    } catch (SQLException e) {
      db.rollback(conn);
      System.out.println("사용자 활성화 실패: " + e.getMessage());
    } finally {
      db.close(pstmt); db.close(conn);
    }
    return result;
  }

  // ─────────────────────────────────────────────────────
  // 5. 권한 변경 (USER ↔ MIDDLEADMIN)
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
  // 6. 비밀번호 변경
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
  // 7. 아이디 중복 체크
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