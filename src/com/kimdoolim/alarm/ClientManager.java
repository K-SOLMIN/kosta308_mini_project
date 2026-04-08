package com.kimdoolim.alarm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager extends Thread{
    private Socket socket;
    private int currentUserId = -1;
    private static final Map<Integer, PrintWriter> clientMap = new ConcurrentHashMap<>();

    public ClientManager(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                // 규격 확인 -> "ID:메시지" 또는 최초 접속 시 "LOGIN:ID"
                if (line.contains(":")) {
                    String[] split = line.split(":", 2);
                    String receiverId = split[0];
                    String content = split[1];

                    // 1. 최초 접속 처리 (맵 등록)
                    if (receiverId.equals("LOGIN")) {
                        this.currentUserId = Integer.parseInt(content);
                        clientMap.put(currentUserId, out);
                        System.out.println("🔑 [로그인] User " + currentUserId + " 연결됨 (현재 접속자: " + clientMap.size() + "명)");
                    }
                    // 2. 알림 배달 처리 (ID:메시지)
                    else {
                        int targetId = Integer.parseInt(receiverId);
                        PrintWriter targetOut = clientMap.get(targetId);

                        if (targetOut != null) {
                            targetOut.println(content); // 실시간 배달
                            System.out.println("📩 [전송] " + targetId + "에게 메시지 배달 완료");
                        } else {
                            System.out.println("⚠️ [실패] " + targetId + "번 유저는 현재 오프라인입니다.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("🔌 [연결 끊김] User " + currentUserId + " 접속 종료");
        } finally {
            if (currentUserId != -1) clientMap.remove(currentUserId);
        }
    }
}
