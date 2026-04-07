package com.kimdoolim.manager;

import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.EquipmentDetail;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentController {
    private static final EquipmentController equipmentController = new EquipmentController();
    private final MySql mysql = MySql.getMySql();

    private EquipmentController() {}

    public static EquipmentController getEquipmentController() {
        return equipmentController;
    }

    public int enrollEquipment(Equipment equipment, List<EquipmentDetail> details) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int result = 0;
        Connection conn = mysql.getConnection();

        String sql = "INSERT INTO equipment (manager_id, name, location, check_delete) VALUES (?, ?, ?, ?)";

        try {
            pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, equipment.getUser().getUserId());
            pstmt.setString(2, equipment.getName());
            pstmt.setString(3, equipment.getLocation());
            pstmt.setBoolean(4, equipment.isCheckDelete());

            result = pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                long equipmentId = rs.getLong(1);

                // 낱개 등록
                String detailSql = "INSERT INTO equipment_detail (equipment_id, check_delete, serial_no, status) VALUES (?, ?, ?, ?)";
                PreparedStatement detailPstmt = conn.prepareStatement(detailSql);

                for (EquipmentDetail detail : details) {
                    detailPstmt.setLong(1, equipmentId);
                    detailPstmt.setBoolean(2, detail.isCheckDelete());
                    detailPstmt.setString(3, detail.getSerialNo());
                    detailPstmt.setString(4, detail.getStatus());
                    detailPstmt.addBatch();
                }
                detailPstmt.executeBatch();
                mysql.close(detailPstmt);
            }

            conn.commit();
            System.out.println(">> 비품 및 낱개 등록 완료!");

        } catch (SQLException e) {
            mysql.rollback(conn);
            System.out.println(">> 비품 등록 실패: " + e.getMessage());
        } finally {
            mysql.close(rs);
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return result;
    }

    public Equipment getEquipmentByName(String name) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Equipment equipment = null;
        Connection conn = mysql.getConnection();

        String sql = "SELECT * FROM equipment WHERE name = ? AND check_delete = FALSE";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                equipment = Equipment.builder()
                        .equipmentId(rs.getLong("equipment_id"))
                        .name(rs.getString("name"))
                        .location(rs.getString("location"))
                        .checkDelete(rs.getBoolean("check_delete"))
                        .status(rs.getString("status"))
                        .build();
            }

        } catch (SQLException e) {
            System.out.println(">> 비품 조회 실패: " + e.getMessage());
        } finally {
            mysql.close(rs);
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return equipment;
    }

    public List<EquipmentDetail> getEquipmentDetails(long equipmentId) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<EquipmentDetail> details = new ArrayList<>();
        Connection conn = mysql.getConnection();

        String sql = "SELECT * FROM equipment_detail WHERE equipment_id = ? AND check_delete = FALSE";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, equipmentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                details.add(EquipmentDetail.builder()
                        .equipmentDetailId(rs.getLong("equipment_detail_id"))
                        .serialNo(rs.getString("serial_no"))
                        .status(rs.getString("status"))
                        .checkDelete(rs.getBoolean("check_delete"))
                        .build());
            }

        } catch (SQLException e) {
            System.out.println(">> 낱개 조회 실패: " + e.getMessage());
        } finally {
            mysql.close(rs);
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return details;
    }

    public int updateEquipment(Equipment equipment) {
        PreparedStatement pstmt = null;
        int result = 0;
        Connection conn = mysql.getConnection();

        String sql = "UPDATE equipment SET name = ?, location = ? WHERE equipment_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, equipment.getName());
            pstmt.setString(2, equipment.getLocation());
            pstmt.setLong(3, equipment.getEquipmentId());

            result = pstmt.executeUpdate();
            conn.commit();
            System.out.println(">> 비품 수정 완료!");

        } catch (SQLException e) {
            mysql.rollback(conn);
            System.out.println(">> 비품 수정 실패: " + e.getMessage());
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return result;
    }

    public int updateEquipmentDetail(EquipmentDetail detail) {
        PreparedStatement pstmt = null;
        int result = 0;
        Connection conn = mysql.getConnection();

        String sql = "UPDATE equipment_detail SET serial_no = ?, status = ? WHERE equipment_detail_id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, detail.getSerialNo());
            pstmt.setString(2, detail.getStatus());
            pstmt.setLong(3, detail.getEquipmentDetailId());

            result = pstmt.executeUpdate();
            conn.commit();
            System.out.println(">> 낱개 수정 완료!");

        } catch (SQLException e) {
            mysql.rollback(conn);
            System.out.println(">> 낱개 수정 실패: " + e.getMessage());
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return result;
    }

    public int deleteEquipment(long equipmentId) {
        PreparedStatement pstmt = null;
        int result = 0;
        Connection conn = mysql.getConnection();

        String equipmentSql = "UPDATE equipment SET check_delete = TRUE, deletedate = NOW() WHERE equipment_id = ?";
        String detailSql = "UPDATE equipment_detail SET check_delete = TRUE, deletedate = NOW() WHERE equipment_id = ?";

        try {
            pstmt = conn.prepareStatement(equipmentSql);
            pstmt.setLong(1, equipmentId);
            result = pstmt.executeUpdate();
            mysql.close(pstmt);

            pstmt = conn.prepareStatement(detailSql);
            pstmt.setLong(1, equipmentId);
            pstmt.executeUpdate();

            conn.commit();
            System.out.println(">> 비품 및 낱개 전체 삭제 완료!");

        } catch (SQLException e) {
            mysql.rollback(conn);
            System.out.println(">> 비품 삭제 실패: " + e.getMessage());
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }

        return result;
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
}