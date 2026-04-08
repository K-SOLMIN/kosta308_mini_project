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

            //클라이언트가 로그인후 소켓에 접속하자마자 보내는 자신의 userId
            String firstLine = in.readLine();
            if (firstLine != null) {
                this.currentUserId = Integer.parseInt(firstLine.trim());
                clientMap.put(currentUserId, out);
                System.out.println("🔑 [연결성공] User " + currentUserId + " (현재 " + clientMap.size() + "명 접속 중)");
            }

            // 2. 그 다음부터는 루프 돌면서 "대상ID:내용" 알림만 배달
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains(":")) {
                    String[] split = line.split(":", 2);
                    int targetId = Integer.parseInt(split[0]);
                    String content = split[1];

                    PrintWriter receiverSocket = clientMap.get(targetId);
                    if (receiverSocket != null) {
                        receiverSocket.println(content);
                        System.out.println("📩 [전달] " + currentUserId + " -> " + targetId + " : " + content);
                    } else {
                        System.out.println("⚠️ [부재중] " + targetId + "번 유저가 오프라인입니다.");
                    }
                } else {
                    System.out.println("소켓으로 보내는 문자열에 이상이 있습니다..");
                }
            }
        } catch (Exception e) {
            System.out.println("🔌 [접속종료] User " + currentUserId);
        } finally {
            if (currentUserId != -1) clientMap.remove(currentUserId);
        }
    }
}
