package com.kimdoolim.socket;

import com.kimdoolim.alarm.AlarmScheduler;
import com.kimdoolim.alarm.AlarmService;
import com.kimdoolim.dto.Reservation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketSession extends Thread{
    private Socket socket;
    private int socketUserId = -1; //서버소켓에 접속한 userId
    AlarmService alarmService = AlarmService.getAlarmService();
    private static final Map<Integer, PrintWriter> clientMap = new ConcurrentHashMap<>();


    public static Map<Integer, PrintWriter> getClientMap() {
        return clientMap;
    }

    public SocketSession(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            //클라이언트가 로그인후 소켓에 접속하자마자 보내는 자신의 userId를 받아서 map에저장
            String firstLine = in.readLine();
            if (firstLine != null) {
                this.socketUserId = Integer.parseInt(firstLine.trim());
                clientMap.put(socketUserId, out);
                System.out.println("🔑 [연결성공] User " + socketUserId + " (현재 " + clientMap.size() + "명 접속 중)");
            }

            // receiverId : 알림내용 을 :로 구분해서 map.get(receiverId)로 PrintWriter가져와서 해당유저에게 알림전달
            //
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("APPROVE_RESERVATION:")) {
                    //요청결과알림
                    addApprovedSchedule(line);
                } else if (line.startsWith("REQUEST_RESERVATION")) {
                    long resId = Long.parseLong(line.split(":")[1]);

                    // 1. 해당 예약의 담당 매니저 ID 조회
                    int managerId = alarmService.getManagerIdByReservationId(resId);

                    if (managerId != -1) {
                        // 2. 매니저의 출력 스트림(PrintWriter) 가져오기
                        PrintWriter managerOut = clientMap.get(managerId);

                        // 16진수 이모지: 📩 (0x1F4E9)
                        String msg = "\ud83d\udce9 [예약 요청] 관리 중인 자원에 새로운 예약 신청(ID: " + resId + ")이 들어왔습니다.";

                        if (managerOut != null) {
                            managerOut.println(msg);
                            System.out.println("📧 [매니저 알림] 예약 " + resId + " -> 매니저 " + managerId + "에게 전송");
                        } else {
                            // 매니저가 오프라인일 때 DB에 저장하는 로직이 있다면 여기 추가
                            alarmService.sendAndSaveAlarm(managerId, msg, "REQUEST");
                            System.out.println("⚠️ [매니저 부재] 매니저 " + managerId + " 오프라인. DB 저장됨.");
                        }
                    } else {
                        System.out.println("❌ [알림 실패] 예약 " + resId + "의 매니저 정보를 찾을 수 없습니다.");
                    }
                } else if (line.startsWith("CANCEL:")) {
                    // 스케줄 취소
                    long resId = Long.parseLong(line.split(":", 2)[1]);
                    AlarmScheduler.getAlarmScheduler().cancelReservationAlarm(resId);
                    System.out.println("❌ [스케줄 취소] 예약 ID: " + resId);
                } else if (line.startsWith("USE_START:")) {
                    String resIdStr = line.split(":")[1];
                    long reservationId = Long.parseLong(resIdStr);

                    System.out.println("🚀 [사용 시작] 예약 ID " + reservationId + " 연체 감지 스케줄 등록");

                    // 연체 알림 전담 스케줄러 호출
                    AlarmScheduler.getAlarmScheduler().scheduleOverdueAlarm(reservationId);
                } else if (line.contains(":")) {
                    sendingAlarm(line);
                } else {
                    System.out.println("소켓으로 보내는 문자열에 이상이 있습니다..");
                }
            }
        } catch (Exception e) {
            System.out.println("🔌 [접속종료] User " + socketUserId);
        } finally {
            if (socketUserId != -1) clientMap.remove(socketUserId);
        }
    }

    private void sendingAlarm(String line) {
        String[] split = line.split(":", 2);
        int alarmReceiverId = Integer.parseInt(split[0]);
        String content = split[1];

        PrintWriter receiverSocket = clientMap.get(alarmReceiverId);
        if (receiverSocket != null) {
            receiverSocket.println(content);
            System.out.println("📩 [전달] " + socketUserId + " -> " + alarmReceiverId + " : " + content);
        } else {
            System.out.println("⚠️ [부재중] " + alarmReceiverId + "번 유저가 오프라인입니다.");
        }
    }

    private void addApprovedSchedule(String line) {
        String reservationId = line.split(":", 2)[1];
        long resId = Long.parseLong(reservationId);

        Reservation reservation = alarmService.getReservationById(resId);
        if (reservation != null) {
            AlarmScheduler.getAlarmScheduler().addReservationAlarm(reservation);
            System.out.println("📅 [스케줄 등록] 예약 ID: " + resId);
        } else {
            System.out.println("⚠️ [스케줄 등록 실패] 예약 ID: " + resId + " 조회 실패");
        }
    }
}
