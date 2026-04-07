package com.kimdoolim.manager;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class BlockPeriodController {
    private static final BlockPeriodController instance = new BlockPeriodController();
    private final Database mysql = MySql.getMySql();

    private BlockPeriodController() {}
    public static BlockPeriodController getBlockPeriodController() { return instance; }

    // 목록 조회
    public List<Map<String, Object>> getAllBlockMasters() {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement("SELECT * FROM block_period ORDER BY block_period_id DESC");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getLong("block_period_id"));
                map.put("start", rs.getDate("block_period_startdate").toLocalDate());
                map.put("end", rs.getDate("block_period_enddate").toLocalDate());
                map.put("desc", rs.getString("block_period_description"));
                list.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return list;
    }

    // 마스터 생성 (만들어만 두기)
    public int enrollBlockMaster(LocalDate start, LocalDate end, String desc) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("INSERT INTO block_period (block_period_startdate, block_period_enddate, block_period_description) VALUES (?, ?, ?)");
            pstmt.setDate(1, java.sql.Date.valueOf(start));
            pstmt.setDate(2, java.sql.Date.valueOf(end));
            pstmt.setString(3, desc);
            int res = pstmt.executeUpdate();
            conn.commit();
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // 일괄 적용 (All items) - EQUIPMENT 테이블명 수정
    public int applyBlockToAll(long masterId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        int count = 0;
        try {
            conn.setAutoCommit(false);
            // 시설 전체 추가
            pstmt = conn.prepareStatement("INSERT INTO block_period_detail (block_period_id, facility_id) SELECT ?, facility_id FROM facility");
            pstmt.setLong(1, masterId);
            count += pstmt.executeUpdate();
            mysql.close(pstmt);

            // 비품 전체 추가 (EQUIPMENT_MASTER -> EQUIPMENT 수정)
            pstmt = conn.prepareStatement("INSERT INTO block_period_detail (block_period_id, equipment_id) SELECT ?, equipment_id FROM equipment");
            pstmt.setLong(1, masterId);
            count += pstmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) { mysql.rollback(conn); }
        finally { mysql.close(pstmt); mysql.close(conn); }
        return count;
    }

    // 개별 적용
    public int enrollBlockDetail(long masterId, String type, long targetId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        String sql = type.equals("F") ?
                "INSERT INTO block_period_detail (block_period_id, facility_id) VALUES (?, ?)" :
                "INSERT INTO block_period_detail (block_period_id, equipment_id) VALUES (?, ?)";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, masterId);
            pstmt.setLong(2, targetId);
            int res = pstmt.executeUpdate();
            conn.commit();
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // 단건 조회
    public Map<String, Object> getBlockPeriodByDescription(String desc) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Object> map = null;
        try {
            pstmt = conn.prepareStatement("SELECT * FROM block_period WHERE block_period_description = ?");
            pstmt.setString(1, desc);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                map = new HashMap<>();
                map.put("id", rs.getLong("block_period_id"));
                map.put("start", rs.getDate("block_period_startdate").toLocalDate());
                map.put("end", rs.getDate("block_period_enddate").toLocalDate());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return map;
    }

    // 수정
    public int updateBlockMaster(String oldDesc, LocalDate start, LocalDate end, String nDesc) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("UPDATE block_period SET block_period_startdate=?, block_period_enddate=?, block_period_description=? WHERE block_period_description=?");
            pstmt.setDate(1, java.sql.Date.valueOf(start));
            pstmt.setDate(2, java.sql.Date.valueOf(end));
            pstmt.setString(3, nDesc);
            pstmt.setString(4, oldDesc);
            int res = pstmt.executeUpdate();
            conn.commit();
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    // 삭제
    public int deleteBlockMasterByDesc(String desc) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("DELETE FROM block_period WHERE block_period_description = ?");
            pstmt.setString(1, desc);
            int res = pstmt.executeUpdate();
            conn.commit();
            return res;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }
}