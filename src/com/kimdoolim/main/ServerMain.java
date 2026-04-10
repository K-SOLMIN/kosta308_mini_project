package com.kimdoolim.main;

import com.kimdoolim.alarm.AlarmScheduler;
import com.kimdoolim.socket.SocketSession;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    private static final int PORT = 9999;

    public static void main(String[] args) {
        AlarmScheduler alarmScheduler = AlarmScheduler.getAlarmScheduler();
        alarmScheduler.startSchedule();

        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("🚀 [알림 서버] 포트 " + PORT + "에서 대기 중...");

            while (true) {
                Socket socket = serverSocket.accept();
                // 클라이언트 접속 시 전담 스레드 생성
                new Thread(new SocketSession(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
