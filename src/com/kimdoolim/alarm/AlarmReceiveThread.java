package com.kimdoolim.alarm;

import com.kimdoolim.common.MySql;
import com.kimdoolim.main.ClientMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AlarmReceiveThread extends Thread{

    @Override
    public void run() {

        System.out.println("알람받을 thread생성");

        try {
            BufferedReader msgReader = new BufferedReader(new InputStreamReader(ClientMain.socket.getInputStream(), StandardCharsets.UTF_8));
            String alarmMsg = "";
            int count = 0;
            int reservationId = 62;

            while((alarmMsg = msgReader.readLine()) != null) {
                System.out.println("\n" + alarmMsg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("알림받는 thread 소멸");
    }

    //즉시사용테스트
    public void insertTestReturnRequests(int count) {
        if(count == 3) return;

        try (Connection conn = MySql.getMySql().getConnection()) {
            // 1. 수동 커밋 모드로 전환 (안전장치)
            conn.setAutoCommit(false);

            String sql = "INSERT INTO return_request (reservation_id, `condition`, status, created_at) VALUES (?, ?, ?, NOW())";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int id = 62; id <= 64; id++) {
                    pstmt.setInt(1, id);
                    pstmt.setString(2, "정상");
                    pstmt.setString(3, (id == 64) ? "TRUE" : "FALSE");

                    pstmt.addBatch(); // 장바구니 담기
                }

                pstmt.executeBatch(); // 장바구니 던지기
                conn.commit();        // [핵심] DB에 최종 승인!

                System.out.println("✅ [테스트] 62(연체), 63(연체), 64(반납됨) 데이터 삽입 및 커밋 완료!");

            } catch (SQLException e) {
                conn.rollback(); // 에러 나면 장바구니 비우고 취소
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 테스트 데이터 삽입 실패: " + e.getMessage());
        }
    }
}
