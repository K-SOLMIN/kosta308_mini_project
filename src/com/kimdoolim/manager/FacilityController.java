package com.kimdoolim.manager;

import com.kimdoolim.common.Auth;
import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FacilityController {
    private static final FacilityController facilityController = new FacilityController();
    Database mysql = MySql.getMySql();
    private FacilityController() { }

    public static FacilityController getFacilityController() {
        return facilityController;
    }

    public User getUserById(String id) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;
        Connection conn = mysql.getConnection();

        String sql = "SELECT * FROM user WHERE id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = User.builder()
                        .userId(rs.getInt("user_id"))
                        .schoolId(rs.getInt("school_id"))
                        .id(rs.getString("id"))
                        .password(rs.getString("password"))
                        .permission(Permission.valueOf(rs.getString("permission")))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .gradeNo(rs.getInt("grade_no"))
                        .classNo(rs.getInt("class_no"))
                        .isActive(Boolean.parseBoolean(rs.getString("is_active")))
                        .build();
            }

        } catch (SQLException e) {
            System.out.println(">> 사용자 조회 실패: " + e.getMessage());
        } finally {
            mysql.close(rs);
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return user;
    }

    public int updatePermission(int userId, Permission permission) {
        PreparedStatement pstmt = null;
        int result = 0;
        Connection conn = mysql.getConnection();

        String sql = "UPDATE user SET permission = ? WHERE user_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, permission.name());
            pstmt.setInt(2, userId);

            result = pstmt.executeUpdate();
            conn.commit();
            System.out.println(">> 권한 변경 완료!");

        } catch (SQLException e) {
            mysql.rollback(conn);
            System.out.println(">> 권한 변경 실패: " + e.getMessage());
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return result;
    }

    public int enrollFacility(Facility facility) {
        PreparedStatement pstmt = null;
        int result = 0;
        Connection conn = mysql.getConnection();

        String sql = "INSERT INTO facility (manager_id, location, name, max_capacity, max_reservation_unit, max_reservation_value, is_delete, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, facility.getUser().getUserId());
            pstmt.setString(2, facility.getLocation());
            pstmt.setString(3, facility.getName());
            pstmt.setInt(4, facility.getMaxCapacity());
            pstmt.setString(5, facility.getMaxReservationUnit());
            pstmt.setInt(6, facility.getMaxReservationValue());
            pstmt.setBoolean(7, facility.isDelete());
            pstmt.setString(8, facility.getStatus());

            result = pstmt.executeUpdate();
            conn.commit();
            System.out.println(">> 시설 등록 완료!");

        } catch (SQLException e) {
            mysql.rollback(conn);
            System.out.println(">> 시설 등록 실패: " + e.getMessage());
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return result;
    }

    public Facility getFacilityByName(String name) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Facility facility = null;
        Connection conn = mysql.getConnection();
        User loginUser = Auth.getUserInfo();

        String sql = "SELECT * FROM facility WHERE name = ? AND manager_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, loginUser.getUserId());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                facility = Facility.builder()
                        .facilityId(rs.getInt("facility_id"))
                        .location(rs.getString("location"))
                        .name(rs.getString("name"))
                        .maxCapacity(rs.getInt("max_capacity"))
                        .maxReservationUnit(rs.getString("max_reservation_unit"))
                        .maxReservationValue(rs.getInt("max_reservation_value"))
                        .status(rs.getString("status"))
                        .build();
            }

        } catch (SQLException e) {
            System.out.println(">> 시설 조회 실패: " + e.getMessage());
        } finally {
            mysql.close(rs);
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return facility;
    }

    public int updateFacility(Facility facility) {
        PreparedStatement pstmt = null;
        int result = 0;
        Connection conn = mysql.getConnection();

        String sql = "UPDATE facility SET manager_id = ?, location = ?, name = ?, " +
                "max_capacity = ?, max_reservation_unit = ?, max_reservation_value = ?, status = ? " +
                "WHERE facility_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, facility.getUser().getUserId());
            pstmt.setString(2, facility.getLocation());
            pstmt.setString(3, facility.getName());
            pstmt.setInt(4, facility.getMaxCapacity());
            pstmt.setString(5, facility.getMaxReservationUnit());
            pstmt.setInt(6, facility.getMaxReservationValue());
            pstmt.setString(7, facility.getStatus());
            pstmt.setLong(8, facility.getFacilityId());

            result = pstmt.executeUpdate();
            conn.commit();
            System.out.println(">> 시설 수정 완료!");

        } catch (SQLException e) {
            mysql.rollback(conn);
            System.out.println(">> 시설 수정 실패: " + e.getMessage());
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return result;
    }
}
