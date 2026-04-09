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
                System.out.println("socketReadLine" + line);
                if (line.startsWith("RESERVATION_RESULT:")) {
                    // 규격 예시: RESERVATION_RESULT:74:APPROVE (승인) 또는 RESERVATION_RESULT:74:REJECT (반려)
                    String[] parts = line.split(":");
                    long resId = Long.parseLong(parts[1]);
                    String status = parts[2]; // "APPROVE" 또는 "REJECT"

                    processReservationResult(resId, status);
                } else if (line.startsWith("REQUEST_RESERVATION")) {
                    FacilityEquipmentRequestAlarm(line);
                } else if (line.startsWith("CANCEL:")) {
                    // 1. 예약 ID 추출
                    long resId = Long.parseLong(line.split(":", 2)[1]);

                    // 2. 예약 정보 조회 (누구에게 보낼지 알기 위해 필요)
                    Reservation reservation = alarmService.getReservationById(resId);

                    if (reservation != null) {
                        // 3. 기존에 걸려있던 알람 스케줄 취소
                        AlarmScheduler.getAlarmScheduler().cancelReservationAlarm(resId);

                        // 4. 사용자에게 취소 알림 전송 (📩 0x1F4E9)
                        int userId = reservation.getUser().getUserId();
                        String targetName = (reservation.getFacility() != null) ?
                                reservation.getFacility().getName() : reservation.getEquipment().getName();

                        String msg = "\ud83d\udce9 [예약취소] '" + targetName + "' 예약이 취소되었습니다.";

                        // 타입을 '예약안내' 혹은 필요하다면 '취소안내'로 통일
                        alarmService.sendAndSaveAlarm(userId, msg, "예약안내");

                        System.out.println("❌ [스케줄 취소] 예약 ID: " + resId + " (사용자 " + userId + "에게 알림 완료)");
                    } else {
                        System.out.println("⚠️ [취소 실패] 예약 ID " + resId + " 정보를 찾을 수 없습니다.");
                    }
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
            e.printStackTrace();
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

//    private void addApprovedSchedule(String line) {
//        String reservationId = line.split(":", 2)[1];
//        long resId = Long.parseLong(reservationId);
//
//        Reservation reservation = alarmService.getReservationById(resId);
//        if (reservation != null) {
//            AlarmScheduler.getAlarmScheduler().addReservationAlarm(reservation);
//            System.out.println("📅 [스케줄 등록] 예약 ID: " + resId);
//        } else {
//            System.out.println("⚠️ [스케줄 등록 실패] 예약 ID: " + resId + " 조회 실패");
//        }
//    }

    private void FacilityEquipmentRequestAlarm(String line) {
        long resId = Long.parseLong(line.split(":")[1]);

        // 1. 해당 예약의 담당 매니저 ID 조회
        int managerId = alarmService.getManagerIdByReservationId(resId);
        Reservation reservation = alarmService.getReservationById(resId);

        if (managerId != -1) {
            // 2. 매니저의 출력 스트림(PrintWriter) 가져오기
            PrintWriter managerOut = clientMap.get(managerId);

            // 16진수 이모지: 📩 (0x1F4E9)
            String msg = "\ud83d\udce9 [예약요청] " + reservation.getFacility().getName() + " 새로운 예약 신청이 들어왔습니다.";

            if (managerOut != null) {
                managerOut.println(msg);
                System.out.println("📧 [매니저 알림] 예약 " + resId + " -> 매니저 " + managerId + "에게 전송");
            } else {
                // 매니저가 오프라인일 때 DB에 저장하는 로직이 있다면 여기 추가
                alarmService.sendAndSaveAlarm(managerId, msg, "[요청]");
                System.out.println("⚠️ [매니저 부재] 매니저 " + managerId + " 오프라인. DB 저장됨.");
            }
        } else {
            System.out.println("❌ [알림 실패] 예약 " + resId + "의 매니저 정보를 찾을 수 없습니다.");
        }
    }

    private void processReservationResult(long resId, String status) {
        // 1. 상세 예약 정보 조회 (사용자 ID, 시설명 등을 알기 위해)
        Reservation reservation = alarmService.getReservationById(resId);
        if (reservation == null) return;

        int userId = reservation.getUser().getUserId();
        String targetName = (reservation.getFacility() != null) ?
                reservation.getFacility().getName() : reservation.getEquipment().getName();

        if ("APPROVE".equals(status)) {
            // [승인 시 처리]
            // 1. 스케줄러 등록 (오늘 날짜 확인은 addReservationAlarm 내부에서 처리됨)
            AlarmScheduler.getAlarmScheduler().addReservationAlarm(reservation);

            // 2. 즉시 알림 발송 (승인되었다는 사실을 바로 알려줌)
            String msg = "\u2705 [예약결과] '" + targetName + "' 예약이 승인되었습니다!";
            alarmService.sendAndSaveAlarm(userId, msg, "예약안내"); // 타입을 '예약안내'로 통일

            System.out.println("📅 [승인 완료] ID: " + resId + " 스케줄 등록 및 사용자 알림 전송");

        } else if ("REJECT".equals(status)) {
            // [반려 시 처리]
            // 1. 단순 알림 발송
            String msg = "\u274c [예약결과] '" + targetName + "' 예약이 반려되었습니다. 사유를 확인해주세요.";
            alarmService.sendAndSaveAlarm(userId, msg, "예약안내"); // 반려도 예약 결과 안내이므로 통일

            System.out.println("❌ [반려 완료] ID: " + resId + " 사용자에게 반려 알림 전송");
        }
    }
}
