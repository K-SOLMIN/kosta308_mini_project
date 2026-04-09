package com.kimdoolim.manager;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class BlockPeriodController {
    private static final BlockPeriodController instance = new BlockPeriodController();
    private final Database mysql = MySql.getMySql();

    private BlockPeriodController() {}
    public static BlockPeriodController getBlockPeriodController() { return instance; }

    // ─────────────────────────────────────────────────────
    // 1. 목록 조회 (교시명 포함)
    // ─────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllBlockMasters() {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT bp.block_period_id, bp.block_period_startdate, " +
                "bp.block_period_enddate, bp.block_period_description, " +
                "p.period_name " +
                "FROM block_period bp " +
                "LEFT JOIN period p ON bp.period_id = p.period_id " +
                "ORDER BY bp.block_period_id ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getLong("block_period_id"));
                map.put("start", rs.getDate("block_period_startdate").toLocalDate());
                map.put("end", rs.getDate("block_period_enddate").toLocalDate());
                map.put("desc", rs.getString("block_period_description"));
                map.put("periodName", rs.getString("period_name")); // null이면 종일
                list.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return list;
    }

    // ─────────────────────────────────────────────────────
    // 2. 마스터 생성 (period_id nullable — null이면 종일 제한)
    // ─────────────────────────────────────────────────────
    public int enrollBlockMaster(LocalDate start, LocalDate end, String desc, Integer periodId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "INSERT INTO block_period " +
                "(block_period_startdate, block_period_enddate, block_period_description, period_id) " +
                "VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            pstmt.setString(3, desc);
            if (periodId != null) pstmt.setInt(4, periodId);
            else pstmt.setNull(4, Types.INTEGER);
            int res = pstmt.executeUpdate();
            mysql.commit(conn);
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // ─────────────────────────────────────────────────────
    // 3. 전체 시설/비품 일괄 차단
    // ─────────────────────────────────────────────────────
    public int applyBlockToAll(long masterId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        int count = 0;
        try {
            pstmt = conn.prepareStatement(
                "INSERT INTO block_period_detail (block_period_id, facility_id) " +
                    "SELECT ?, facility_id FROM facility");
            pstmt.setLong(1, masterId);
            count += pstmt.executeUpdate();
            mysql.close(pstmt);

            pstmt = conn.prepareStatement(
                "INSERT INTO block_period_detail (block_period_id, equipment_id) " +
                    "SELECT ?, equipment_id FROM equipment");
            pstmt.setLong(1, masterId);
            count += pstmt.executeUpdate();

            mysql.commit(conn);
        } catch (SQLException e) { mysql.rollback(conn); }
        finally { mysql.close(pstmt); mysql.close(conn); }
        return count;
    }

    // ─────────────────────────────────────────────────────
    // 4. 개별 시설/비품 차단
    // ─────────────────────────────────────────────────────
    public int enrollBlockDetail(long masterId, String type, long targetId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        String sql = type.equals("F")
            ? "INSERT INTO block_period_detail (block_period_id, facility_id) VALUES (?, ?)"
            : "INSERT INTO block_period_detail (block_period_id, equipment_id) VALUES (?, ?)";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, masterId);
            pstmt.setLong(2, targetId);
            int res = pstmt.executeUpdate();
            mysql.commit(conn);
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // ─────────────────────────────────────────────────────
    // 5. 단건 조회
    // ─────────────────────────────────────────────────────
    public Map<String, Object> getBlockPeriodByDescription(String desc) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Object> map = null;
        try {
            String sql = "SELECT bp.block_period_id, bp.block_period_startdate, " +
                "bp.block_period_enddate, bp.period_id, p.period_name " +
                "FROM block_period bp " +
                "LEFT JOIN period p ON bp.period_id = p.period_id " +
                "WHERE bp.block_period_description = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, desc);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                map = new HashMap<>();
                map.put("id",         rs.getLong("block_period_id"));
                map.put("start",      rs.getDate("block_period_startdate").toLocalDate());
                map.put("end",        rs.getDate("block_period_enddate").toLocalDate());
                map.put("periodId",   rs.getObject("period_id"));   // null 가능
                map.put("periodName", rs.getString("period_name")); // null이면 종일
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return map;
    }

    // ─────────────────────────────────────────────────────
    // 5-2. ID로 단건 조회
    // ─────────────────────────────────────────────────────
    public Map<String, Object> getBlockPeriodById(long id) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Object> map = null;
        try {
            String sql = "SELECT bp.block_period_id, bp.block_period_startdate, " +
                "bp.block_period_enddate, bp.block_period_description, bp.period_id, p.period_name " +
                "FROM block_period bp " +
                "LEFT JOIN period p ON bp.period_id = p.period_id " +
                "WHERE bp.block_period_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                map = new HashMap<>();
                map.put("id",         rs.getLong("block_period_id"));
                map.put("start",      rs.getDate("block_period_startdate").toLocalDate());
                map.put("end",        rs.getDate("block_period_enddate").toLocalDate());
                map.put("desc",       rs.getString("block_period_description"));
                map.put("periodId",   rs.getObject("period_id"));
                map.put("periodName", rs.getString("period_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return map;
    }

    // ─────────────────────────────────────────────────────
    // 6. 수정 (ID 기준, period_id 포함)
    // ─────────────────────────────────────────────────────
    public int updateBlockMasterById(long id, LocalDate start, LocalDate end,
                                     String nDesc, Integer periodId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "UPDATE block_period " +
                "SET block_period_startdate=?, block_period_enddate=?, " +
                "    block_period_description=?, period_id=? " +
                "WHERE block_period_id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            pstmt.setString(3, nDesc);
            if (periodId != null) pstmt.setInt(4, periodId);
            else pstmt.setNull(4, Types.INTEGER);
            pstmt.setLong(5, id);
            int res = pstmt.executeUpdate();
            mysql.commit(conn);
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    public int updateBlockMaster(String oldDesc, LocalDate start, LocalDate end,
                                 String nDesc, Integer periodId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "UPDATE block_period " +
                "SET block_period_startdate=?, block_period_enddate=?, " +
                "    block_period_description=?, period_id=? " +
                "WHERE block_period_description=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(start));
            pstmt.setDate(2, Date.valueOf(end));
            pstmt.setString(3, nDesc);
            if (periodId != null) pstmt.setInt(4, periodId);
            else pstmt.setNull(4, Types.INTEGER);
            pstmt.setString(5, oldDesc);
            int res = pstmt.executeUpdate();
            mysql.commit(conn);
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // ─────────────────────────────────────────────────────
    // 7. 삭제 — detail 먼저 삭제 후 master 삭제 (FK 오류 방지)
    // ─────────────────────────────────────────────────────
    public int deleteBlockMasterByDesc(String desc) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            // 7-1. master id 조회
            pstmt = conn.prepareStatement(
                "SELECT block_period_id FROM block_period WHERE block_period_description = ?");
            pstmt.setString(1, desc);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) { mysql.close(rs); return 0; }
            long masterId = rs.getLong("block_period_id");
            mysql.close(rs);
            mysql.close(pstmt);

            // 7-2. detail 먼저 삭제
            pstmt = conn.prepareStatement(
                "DELETE FROM block_period_detail WHERE block_period_id = ?");
            pstmt.setLong(1, masterId);
            pstmt.executeUpdate();
            mysql.close(pstmt);

            // 7-3. master 삭제
            pstmt = conn.prepareStatement(
                "DELETE FROM block_period WHERE block_period_id = ?");
            pstmt.setLong(1, masterId);
            int res = pstmt.executeUpdate();

            mysql.commit(conn);
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // ─────────────────────────────────────────────────────
    // 8. 시설 목록 조회 (id, name, location)
    // ─────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllFacilities() {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(
                "SELECT facility_id, name, location FROM facility WHERE is_delete = 'false' ORDER BY facility_id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id",       rs.getLong("facility_id"));
                map.put("name",     rs.getString("name"));
                map.put("location", rs.getString("location"));
                list.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return list;
    }

    // ─────────────────────────────────────────────────────
    // 9. 비품 목록 조회 (id, name, location)
    // ─────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllEquipments() {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(
                "SELECT equipment_id, name, location FROM equipment WHERE check_delete = 'false' ORDER BY equipment_id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id",       rs.getLong("equipment_id"));
                map.put("name",     rs.getString("name"));
                map.put("location", rs.getString("location"));
                list.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return list;
    }

    // ─────────────────────────────────────────────────────
    // 10. 전체 교시 목록 조회 (View에서 교시 선택 시 사용)
    // ─────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllPeriods() {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(
                "SELECT period_id, period_name, start_time, end_time FROM period ORDER BY period_id");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id",        rs.getInt("period_id"));
                map.put("name",      rs.getString("period_name"));
                map.put("startTime", rs.getTime("start_time").toLocalTime());
                map.put("endTime",   rs.getTime("end_time").toLocalTime());
                list.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return list;
    }
}