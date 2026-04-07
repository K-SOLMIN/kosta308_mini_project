package com.kimdoolim.manager;

import com.kimdoolim.common.Database;
import com.kimdoolim.common.MySql;
import com.kimdoolim.dto.Facility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FacilityController {
    private static final FacilityController facilityController = new FacilityController();
    Database mysql = MySql.getMySql();
    private FacilityController() { }

    public static FacilityController getFacilityController() {
        return facilityController;
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
}
