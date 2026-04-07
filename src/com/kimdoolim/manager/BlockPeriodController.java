package com.kimdoolim.manager;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.BlockPeriod;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlockPeriodController {
    private static final BlockPeriodController instance = new BlockPeriodController();
    private final Database mysql = MySql.getMySql();

    private BlockPeriodController() {}
    public static BlockPeriodController getBlockPeriodController() { return instance; }

    // [1] 모든 제한 일정 조회 (ID 오름차순)
    public List<BlockPeriod> getAllBlockPeriods() {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<BlockPeriod> blockList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM block_period ORDER BY block_period_id ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                blockList.add(BlockPeriod.builder()
                        .blockPeriodId(rs.getInt("block_period_id"))
                        .startDate(rs.getDate("block_period_startdate").toLocalDate())
                        .endDate(rs.getDate("block_period_enddate").toLocalDate())
                        .description(rs.getString("block_period_description"))
                        .build());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return blockList;
    }

    // [2] 제한 일정 등록 (명칭 기반)
    public int enrollBlockPeriod(BlockPeriod blockPeriod) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int generatedId = 0;
        try {
            String sql = "INSERT INTO block_period (block_period_startdate, block_period_enddate, block_period_description) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setDate(1, Date.valueOf(blockPeriod.getStartDate()));
            pstmt.setDate(2, Date.valueOf(blockPeriod.getEndDate()));
            pstmt.setString(3, blockPeriod.getDescription());
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            if (rs.next()) generatedId = rs.getInt(1);
            conn.commit();
        } catch (SQLException e) { mysql.rollback(conn); e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return generatedId;
    }

    // [3] 모든 시설/비품 일괄 차단 (상세 등록)
    public int applyBlockToAllResources(int blockPeriodId) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        int totalCount = 0;
        try {
            conn.setAutoCommit(false);
            // 시설 전체 추가
            pstmt = conn.prepareStatement("INSERT INTO block_period_detail (block_period_id, facility_id) SELECT ?, facility_id FROM facility");
            pstmt.setInt(1, blockPeriodId);
            totalCount += pstmt.executeUpdate();
            mysql.close(pstmt);

            // 비품 전체 추가 (테이블/컬럼명 재확인)
            pstmt = conn.prepareStatement("INSERT INTO block_period_detail (block_period_id, equipment_id) SELECT ?, equipment_id FROM equipment");
            pstmt.setInt(1, blockPeriodId);
            totalCount += pstmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            mysql.rollback(conn);
            e.printStackTrace();
            return 0;
        } finally { mysql.close(pstmt); mysql.close(conn); }
        return totalCount;
    }

    // [4] 명칭(Description)으로 일정 정보 조회
    public BlockPeriod getBlockByDescription(String description) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        BlockPeriod blockPeriod = null;
        try {
            pstmt = conn.prepareStatement("SELECT * FROM block_period WHERE block_period_description = ?");
            pstmt.setString(1, description);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                blockPeriod = BlockPeriod.builder()
                        .blockPeriodId(rs.getInt("block_period_id"))
                        .startDate(rs.getDate("block_period_startdate").toLocalDate())
                        .endDate(rs.getDate("block_period_enddate").toLocalDate())
                        .description(rs.getString("block_period_description"))
                        .build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { mysql.close(rs); mysql.close(pstmt); mysql.close(conn); }
        return blockPeriod;
    }

    // [5] 명칭 기반 수정
    public int updateBlockPeriod(String targetDescription, BlockPeriod updatedData) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;
        try {
            String sql = "UPDATE block_period SET block_period_startdate=?, block_period_enddate=?, block_period_description=? WHERE block_period_description=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, Date.valueOf(updatedData.getStartDate()));
            pstmt.setDate(2, Date.valueOf(updatedData.getEndDate()));
            pstmt.setString(3, updatedData.getDescription());
            pstmt.setString(4, targetDescription);

            int result = pstmt.executeUpdate();
            conn.commit();
            return result;
        } catch (SQLException e) { mysql.rollback(conn); return 0; }
        finally { mysql.close(pstmt); mysql.close(conn); }
    }

    public int applyBlockToSpecificResource(int blockPeriodId, String resourceType, String resourceName) {
        Connection conn = mysql.getConnection();
        PreparedStatement pstmt = null;

        // 타입에 따라 시설 테이블(facility) 또는 비품 테이블(equipment)에서 ID를 조회해 INSERT
        String sql = "";
        if (resourceType.equals("F")) {
            sql = "INSERT INTO block_period_detail (block_period_id, facility_id) " +
                    "SELECT ?, facility_id FROM facility WHERE name = ?";
        } else {
            sql = "INSERT INTO block_period_detail (block_period_id, equipment_id) " +
                    "SELECT ?, equipment_id FROM equipment WHERE name = ?";
        }

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, blockPeriodId);
            pstmt.setString(2, resourceName);

            int result = pstmt.executeUpdate();
            conn.commit();
            return result;
        } catch (SQLException e) {
            mysql.rollback(conn);
            e.printStackTrace();
            return 0;
        } finally {
            mysql.close(pstmt);
            mysql.close(conn);
        }
    }
}