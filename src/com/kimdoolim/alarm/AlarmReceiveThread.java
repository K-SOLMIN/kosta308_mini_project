package com.kimdoolim.alarm;

import com.kimdoolim.main.ClientMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AlarmReceiveThread extends Thread{

    @Override
    public void run() {

        System.out.println("알람받을 thread생성");

        try {
            BufferedReader msgReader = new BufferedReader(new InputStreamReader(ClientMain.socket.getInputStream()));
            String alarmMsg = "";

            while((alarmMsg = msgReader.readLine()) != null) {
                System.out.println(alarmMsg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("알림받는 thread 소멸");
    }
}
