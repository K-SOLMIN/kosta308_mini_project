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
                if (line.equals("PING")) continue;

                System.out.println("socketReadLine : " + line);

                if (line.startsWith("RESERVATION_RESULT:")) {
                    String[] parts = line.split(":");
                    long resId = Long.parseLong(parts[1]);
                    String status = parts[2];
                    processReservationResult(resId, status);

                } else if (line.startsWith("REQUEST_RESERVATION:")) {
                    facilityEquipmentRequestAlarm(line);

                } else if (line.startsWith("CANCEL:")) {
                    String[] parts = line.split(":");
                    long resId = Long.parseLong(parts[1]);
                    String cancelType = parts.length > 2 ? parts[2] : "USER";

                    Reservation reservation = alarmService.getReservationById(resId);

                    if (reservation != null) {
                        // 사용 + 반납 + 연체 스케줄 전부 취소
                        AlarmScheduler.getAlarmScheduler().cancelReservationAlarm(resId);

                        if ("ADMIN".equals(cancelType)) {
                            // 관리자 강제취소 - 사용자에게 알림
                            int userId = reservation.getUser().getUserId();
                            String targetName = (reservation.getFacility() != null)
                                    ? reservation.getFacility().getName()
                                    : reservation.getEquipment().getName();
                            String targetType = (reservation.getFacility() != null) ? "시설" : "비품";

                            String msgToUser = "\ud83d\udce9 [예약취소] " + targetType + " '" + targetName + "' 예약이 관리자에 의해 취소되었습니다.";
                            alarmService.sendAndSaveAlarm(userId, msgToUser, "예약안내");
                            System.out.println("❌ [관리자 강제취소] 예약 ID: " + resId + " 사용자 " + userId + "에게 알림 완료");

                        } else {
                            // 사용자 본인 취소 - 알림 없음
                            System.out.println("❌ [사용자 취소] 예약 ID: " + resId + " 스케줄만 취소");
                        }

                    } else {
                        System.out.println("⚠️ [취소 실패] 예약 ID " + resId + " 정보를 찾을 수 없습니다.");
                    }
            } else if (line.startsWith("RETURN_COMPLETE:")) {
                    long resId = Long.parseLong(line.split(":", 2)[1]);
                    AlarmScheduler.getAlarmScheduler().cancelReservationAlarm(resId);
                    System.out.println("✅ [반납완료] 예약 ID: " + resId + " 반납알림 + 연체알림 스케줄 취소");
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

    private void facilityEquipmentRequestAlarm(String line) {
        long resId = Long.parseLong(line.split(":")[1]);

// 1. 해당 예약의 담당 매니저 ID 및 상세 정보 조회
        int managerId = alarmService.getManagerIdByReservationId(resId);
        Reservation reservation = alarmService.getReservationById(resId);

        if (managerId != -1 && reservation != null) {
            // 2. 시설/비품 이름 판별 (시설이 null이면 비품명을 사용)
            String targetName = (reservation.getFacility() != null)
                    ? reservation.getFacility().getName()
                    : (reservation.getEquipment() != null ? reservation.getEquipment().getName() : "알 수 없는 자원");

            // 3. 매니저의 출력 스트림 가져오기
            PrintWriter managerOut = clientMap.get(managerId);

            // 4. 메시지 구성 (📩 0x1F4E9)
            String msg = "\ud83d\udce9 [예약요청] '" + targetName + "' 새로운 예약 신청이 들어왔습니다.";

            // 5. 발송 및 저장 로직
            if (managerOut != null) {
                managerOut.println(msg);                          // 소켓 전송만
                alarmService.saveAlarmToDb(managerId, msg, "예약요청"); // DB저장만
                System.out.println("📧 [매니저 알림] 예약 " + resId + " (" + targetName + ") -> 매니저 " + managerId + " 전송 완료");
            } else {
                alarmService.sendAndSaveAlarm(managerId, msg, "예약요청"); // 오프라인이면 DB저장만
                System.out.println("⚠️ [매니저 부재] 매니저 " + managerId + " 오프라인. DB 저장됨.");
            }
        } else {
            System.out.println("❌ [알림 실패] 예약 " + resId + "의 정보 또는 매니저를 찾을 수 없습니다.");
        }
    }

    private void processReservationResult(long resId, String status) {
        Reservation reservation = alarmService.getReservationById(resId);
        if (reservation == null) return;

        int userId = reservation.getUser().getUserId();
        String targetName = (reservation.getFacility() != null) ?
                reservation.getFacility().getName() : reservation.getEquipment().getName();
        String targetType = (reservation.getFacility() != null) ? "시설" : "비품";

        if ("APPROVE".equals(status)) {
            String msg = "\u2705 [예약결과] " + targetType + " '" + targetName + "' 예약이 승인되었습니다!";
            alarmService.sendAndSaveAlarm(userId, msg, "예약안내");

            AlarmScheduler.getAlarmScheduler().addReservationAlarm(reservation);
            System.out.println("📅 [승인 완료] ID: " + resId + " 스케줄 등록 및 사용자 알림 전송");

        } else if ("REJECT".equals(status)) {
            String msg = "\u274c [예약결과] " + targetType + " '" + targetName + "' 예약이 반려되었습니다. 사유를 확인해주세요.";
            alarmService.sendAndSaveAlarm(userId, msg, "예약안내");

            System.out.println("❌ [반려 완료] ID: " + resId + " 사용자에게 반려 알림 전송");
        }
    }
}
