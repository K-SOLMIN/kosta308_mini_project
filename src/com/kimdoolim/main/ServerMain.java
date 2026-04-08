package com.kimdoolim.main;

import com.kimdoolim.alarm.AlarmScheduler;
import com.kimdoolim.alarm.AlarmTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMain {
    public static void main(String[] args) {
        AlarmScheduler alarmScheduler = AlarmScheduler.getInstance();
        alarmScheduler.startSchedule(); // 스케줄러 시작

        new AlarmTest().registerAndApprove(); // alarmTest

        Map<Integer, Socket> socketManager = new ConcurrentHashMap<>();
        Map<Integer, PrintWriter> outStreamManager = new ConcurrentHashMap<>();

        try (ServerSocket socket = new ServerSocket(8080);) {
            System.out.println("serverSocket 열림");
            while(true) {
                Socket connection = socket.accept();
                System.out.println("new client connect");

                new Thread(() -> {
                    System.out.println("create thread");
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        Socket receiverSocket = null;

                        String id = br.readLine();
                        socketManager.put(Integer.parseInt(id), connection);
                        outStreamManager.put(Integer.parseInt(id),
                                new PrintWriter(connection.getOutputStream(), true));

                        String msg;
                        while((msg = br.readLine()) != null) {
                            String[] receiveData = msg.split(":");

                            int receiverId = 0;
                            String sendingMsg = "";

                            receiverId = Integer.parseInt(receiveData[0].trim());
                            sendingMsg = receiveData[1].trim();
                            System.out.println("receiverId : " + receiverId);
                            System.out.println("sendingMsg : " + sendingMsg);

                            receiverSocket = socketManager.get(receiverId);
                            if(receiverSocket != null) {
                                outStreamManager.get(receiverId).println(sendingMsg);
                            }
                        }

                        System.out.println("thread stop");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
