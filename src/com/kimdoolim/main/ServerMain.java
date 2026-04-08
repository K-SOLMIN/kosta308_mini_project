package com.kimdoolim.main;

import com.kimdoolim.alarm.AlarmScheduler;
import com.kimdoolim.alarm.AlarmTest;
import com.kimdoolim.alarm.ClientManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMain {

    private static final int PORT = 9999;
    private static final Map<Integer, PrintWriter> clientMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        AlarmScheduler alarmScheduler = AlarmScheduler.getInstance();
        alarmScheduler.startSchedule(); // 스케줄러 시작

        new AlarmTest().registerAndApprove(); // alarmTest

        Map<Integer, Socket> socketManager = new ConcurrentHashMap<>();
        Map<Integer, PrintWriter> outStreamManager = new ConcurrentHashMap<>();

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("🚀 [알림 서버] 포트 " + PORT + "에서 대기 중...");

                while (true) {
                    Socket socket = serverSocket.accept();
                    // 클라이언트 접속 시 전담 스레드 생성
                    new Thread(new ClientManager(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        try (ServerSocket socket = new ServerSocket(8080);) {
//            System.out.println("serverSocket 열림");
//            while(true) {
//                Socket connection = socket.accept();
//                System.out.println("new client connect");
//
//                new Thread(() -> {
//                    System.out.println("create thread");
//                    try {
//                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                        Socket receiverSocket = null;
//
//                        String id = br.readLine();
//                        socketManager.put(Integer.parseInt(id), connection);
//                        outStreamManager.put(Integer.parseInt(id),
//                                new PrintWriter(connection.getOutputStream(), true));
//
//                        String msg;
//                        while((msg = br.readLine()) != null) {
//                            String[] receiveData = msg.split(":");
//
//                            int receiverId = 0;
//                            String sendingMsg = "";
//
//                            receiverId = Integer.parseInt(receiveData[0].trim());
//                            sendingMsg = receiveData[1].trim();
//                            System.out.println("receiverId : " + receiverId);
//                            System.out.println("sendingMsg : " + sendingMsg);
//
//                            receiverSocket = socketManager.get(receiverId);
//                            if(receiverSocket != null) {
//                                outStreamManager.get(receiverId).println(sendingMsg);
//                            }
//                        }
//
//                        System.out.println("thread stop");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

}
