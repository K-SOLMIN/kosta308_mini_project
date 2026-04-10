package com.kimdoolim.alarm;

import com.kimdoolim.main.ClientMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AlarmReceiveThread extends Thread{

    @Override
    public void run() {

        try {
            BufferedReader msgReader = new BufferedReader(new InputStreamReader(ClientMain.socket.getInputStream(), StandardCharsets.UTF_8));
            String alarmMsg = "";
            int count = 0;
            int reservationId = 62;

            while((alarmMsg = msgReader.readLine()) != null) {
                if (alarmMsg.equals("FORCE_LOGOUT")) {
                    System.out.println("\n⚠️ 다른 곳에서 로그인하여 현재 세션이 종료됩니다.");
                    System.exit(0);
                }
                System.out.println("\n" + alarmMsg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("알림받는 thread 소멸");
    }
}
