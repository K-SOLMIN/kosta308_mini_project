package com.kimdoolim.manager;

import com.kimdoolim.alarm.AlarmSendingManager;
import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dao.ReservationDAO;
import com.kimdoolim.dto.Reservation;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class BlockPeriodController {
    private static final BlockPeriodController instance = new BlockPeriodController();
    private final Database mysql = MySql.getMySql();

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final AlarmSendingManager sendingManager = AlarmSendingManager.getAlarmSendingManager();

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

        if (count > 0) cancelAndNotifyForBlock(masterId, null, null);
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
        int res = 0;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, masterId);
            pstmt.setLong(2, targetId);
            res = pstmt.executeUpdate();
            mysql.commit(conn);
        } catch (SQLException e) { mysql.rollback(conn); }
        finally { mysql.close(pstmt); mysql.close(conn); }

        if (res > 0) {
            Long facilityId  = type.equals("F") ? targetId : null;
            Long equipmentId = type.equals("E") ? targetId : null;
            cancelAndNotifyForBlock(masterId, facilityId, equipmentId);
        }
        return res;
    }

    // ─────────────────────────────────────────────────────
    // 제한 일정 등록 후 겹치는 예약 취소 + 사용자 알림
    // ─────────────────────────────────────────────────────
    private void cancelAndNotifyForBlock(long masterId, Long facilityId, Long equipmentId) {
        Map<String, Object> info = getBlockPeriodById(masterId);
        if (info == null) return;

        LocalDate start    = (LocalDate) info.get("start");
        LocalDate end      = (LocalDate) info.get("end");
        Integer periodId   = (Integer)   info.get("periodId");
        String desc        = (String)    info.get("desc");

        String reason = "제한 일정(" + desc + ")으로 인한 자동 취소";
        List<Reservation> cancelled = reservationDAO.cancelReservationsForBlockPeriod(
            start, end, periodId, facilityId, equipmentId, reason);

        for (Reservation r : cancelled) {
            sendingManager.sendingTextToSocketServer("취소", r.getReservationId(), "BLOCK");
        }

        if (!cancelled.isEmpty()) {
            System.out.println("🚫 [제한 일정] " + cancelled.size() + "건의 예약이 자동 취소되었습니다.");
        }
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
    public int deleteBlockMasterById(long id) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            // detail 먼저 삭제
            pstmt = conn.prepareStatement(
                "DELETE FROM block_period_detail WHERE block_period_id = ?");
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            mysql.close(pstmt);

            // master 삭제
            pstmt = conn.prepareStatement(
                "DELETE FROM block_period WHERE block_period_id = ?");
            pstmt.setLong(1, id);
            int res = pstmt.executeUpdate();

            mysql.commit(conn);
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // ─────────────────────────────────────────────────────
    // 7. 적용 대상 조회 (시설/비품 이름 목록 + 전체 여부)
    // ─────────────────────────────────────────────────────
    public Map<String, Object> getBlockDetailsForDisplay(long masterId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> facilities = new ArrayList<>();
        List<String> equipments = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();

        try {
            // 적용된 시설/비품 이름 조회
            String sql = "SELECT f.name AS facility_name, e.name AS equipment_name " +
                "FROM block_period_detail bpd " +
                "LEFT JOIN facility f ON bpd.facility_id = f.facility_id " +
                "LEFT JOIN equipment e ON bpd.equipment_id = e.equipment_id " +
                "WHERE bpd.block_period_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, masterId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String fn = rs.getString("facility_name");
                String en = rs.getString("equipment_name");
                if (fn != null) facilities.add(fn);
                if (en != null) equipments.add(en);
            }
            mysql.close(rs); rs = null;
            mysql.close(pstmt); pstmt = null;

            // 전체 시설/비품 수 조회 → 전체 적용 여부 판단
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

        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return result;
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