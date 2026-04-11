package com.kimdoolim.socket;

import com.kimdoolim.alarm.AlarmScheduler;
import com.kimdoolim.alarm.AlarmService;
import com.kimdoolim.dto.Reservation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketSession extends Thread {

    private final Socket socket;
    private int socketUserId = -1;
    private PrintWriter myWriter; // 중복 로그인 시 finally에서 내 세션만 제거하기 위해 보관
    private final AlarmService alarmService = AlarmService.getAlarmService();

    private static final Map<Integer, PrintWriter> clientMap = new ConcurrentHashMap<>();

    public static Map<Integer, PrintWriter> getClientMap() { return clientMap; }

    public SocketSession(Socket socket) { this.socket = socket; }

    // ─────────────────────────────────────────────────────
    // 메인 루프
    // ─────────────────────────────────────────────────────
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            if (!registerSession(in, out)) return;

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("PING")) continue;
                System.out.println("📨 [수신] " + line);
                handleMessage(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("🔌 [접속종료] User " + socketUserId);
        } finally {
            // 중복 로그인으로 새 세션이 교체한 경우엔 제거하지 않음
            if (socketUserId != -1) clientMap.remove(socketUserId, myWriter);
        }
    }

    // ─────────────────────────────────────────────────────
    // 최초 접속 시 userId 등록 및 중복 로그인 처리
    // ─────────────────────────────────────────────────────
    private boolean registerSession(BufferedReader in, PrintWriter out) throws Exception {
        String firstLine = in.readLine();
        if (firstLine == null) return false;

        this.socketUserId = Integer.parseInt(firstLine.trim());

        PrintWriter existing = clientMap.get(socketUserId);
        if (existing != null) {
            existing.println("FORCE_LOGOUT");
            System.out.println("⚠️ [중복 로그인] User " + socketUserId + " 기존 세션 강제 종료");
        }

        this.myWriter = out;
        clientMap.put(socketUserId, out);
        System.out.println("🔑 [연결성공] User " + socketUserId + " (현재 " + clientMap.size() + "명 접속 중)");
        return true;
    }

    // ─────────────────────────────────────────────────────
    // 수신 메시지 라우팅
    // ─────────────────────────────────────────────────────
    private void handleMessage(String line) {
        if (line.startsWith("RESERVATION_RESULT:")) {
            String[] parts = line.split(":");
            processReservationResult(Long.parseLong(parts[1]), parts[2]);

        } else if (line.startsWith("REQUEST_RESERVATION:")) {
            facilityEquipmentRequestAlarm(line);

        } else if (line.startsWith("CANCEL:")) {
            processCancelAlarm(line);

        } else if (line.startsWith("RETURN_COMPLETE:")) {
            processReturnComplete(line);

        } else if (line.contains(":")) {
            sendingAlarm(line);

        } else {
            System.out.println("⚠️ [파싱 오류] 알 수 없는 메시지 형식: " + line);
        }
    }

    // ─────────────────────────────────────────────────────
    // 예약 취소 처리
    // ─────────────────────────────────────────────────────
    private void processCancelAlarm(String line) {
        String[] parts = line.split(":");
        long resId = Long.parseLong(parts[1]);
        String cancelType = parts.length > 2 ? parts[2] : "USER";

        Reservation reservation = alarmService.getReservationById(resId);
        if (reservation == null) {
            System.out.println("⚠️ [취소 실패] 예약 ID " + resId + " 정보를 찾을 수 없습니다.");
            return;
        }

        AlarmScheduler.getAlarmScheduler().cancelReservationAlarm(resId);

        if ("ADMIN".equals(cancelType)) {
            int userId = reservation.getUser().getUserId();
            String targetType = reservation.getFacility() != null ? "시설" : "비품";
            String targetName = reservation.getFacility() != null
                    ? reservation.getFacility().getName()
                    : reservation.getEquipment().getName();

            String msg = "📩 [예약취소] " + targetType + " '" + targetName + "' 예약이 관리자에 의해 취소되었습니다.";
            alarmService.sendAndSaveAlarm(userId, msg, "예약안내");
            System.out.println("❌ [관리자 강제취소] 예약 ID: " + resId + " → 사용자 " + userId + " 알림 완료");
        } else {
            System.out.println("❌ [사용자 취소] 예약 ID: " + resId + " 스케줄만 취소");
        }
    }

    // ─────────────────────────────────────────────────────
    // 반납 완료 처리
    // ─────────────────────────────────────────────────────
    private void processReturnComplete(String line) {
        long resId = Long.parseLong(line.split(":", 2)[1]);
        AlarmScheduler.getAlarmScheduler().cancelReservationAlarm(resId);
        System.out.println("✅ [반납완료] 예약 ID: " + resId + " 반납알림 + 연체알림 스케줄 취소");
    }

    // ─────────────────────────────────────────────────────
    // 단순 알림 전달 (receiverId:content 형식)
    // ─────────────────────────────────────────────────────
    private void sendingAlarm(String line) {
        String[] split = line.split(":", 2);
        int receiverId = Integer.parseInt(split[0]);
        String content = split[1];

        PrintWriter receiverOut = clientMap.get(receiverId);
        if (receiverOut != null) {
            receiverOut.println(content);
            System.out.println("📩 [전달] " + socketUserId + " → " + receiverId + " : " + content);
        } else {
            System.out.println("⚠️ [부재중] " + receiverId + "번 유저가 오프라인입니다.");
        }
    }

    // ─────────────────────────────────────────────────────
    // 예약 요청 알림 (담당 매니저에게 전달)
    // ─────────────────────────────────────────────────────
    private void facilityEquipmentRequestAlarm(String line) {
        long resId = Long.parseLong(line.split(":")[1]);

        int managerId = alarmService.getManagerIdByReservationId(resId);
        Reservation reservation = alarmService.getReservationById(resId);

        if (managerId == -1 || reservation == null) {
            System.out.println("❌ [알림 실패] 예약 " + resId + "의 정보 또는 매니저를 찾을 수 없습니다.");
            return;
        }

        String targetName = reservation.getFacility() != null
                ? reservation.getFacility().getName()
                : (reservation.getEquipment() != null ? reservation.getEquipment().getName() : "알 수 없는 자원");

        String msg = "📩 [예약요청] '" + targetName + "' 새로운 예약 신청이 들어왔습니다.";
        PrintWriter managerOut = clientMap.get(managerId);

        if (managerOut != null) {
            managerOut.println(msg);
            alarmService.saveAlarmToDb(managerId, msg, "예약요청", true);
            System.out.println("📧 [매니저 알림] 예약 " + resId + " (" + targetName + ") → 매니저 " + managerId + " 전송 완료");
        } else {
            alarmService.sendAndSaveAlarm(managerId, msg, "예약요청");
            System.out.println("⚠️ [매니저 부재] 매니저 " + managerId + " 오프라인. DB 저장됨.");
        }
    }

    // ─────────────────────────────────────────────────────
    // 예약 승인/반려 결과 처리
    // ─────────────────────────────────────────────────────
    private void processReservationResult(long resId, String status) {
        Reservation reservation = alarmService.getReservationById(resId);
        if (reservation == null) return;

        int userId = reservation.getUser().getUserId();
        String targetType = reservation.getFacility() != null ? "시설" : "비품";
        String targetName = reservation.getFacility() != null
                ? reservation.getFacility().getName()
                : reservation.getEquipment().getName();

        if ("APPROVE".equals(status)) {
            String msg = "✅ [예약결과] " + targetType + " '" + targetName + "' 예약이 승인되었습니다!";
            alarmService.sendAndSaveAlarm(userId, msg, "예약안내");
            AlarmScheduler.getAlarmScheduler().addReservationAlarm(reservation);
            System.out.println("📅 [승인 완료] ID: " + resId + " 스케줄 등록 및 사용자 알림 전송");

        } else if ("REJECT".equals(status)) {
            String msg = "❌ [예약결과] " + targetType + " '" + targetName + "' 예약이 반려되었습니다. 사유를 확인해주세요.";
            alarmService.sendAndSaveAlarm(userId, msg, "예약안내");
            System.out.println("❌ [반려 완료] ID: " + resId + " 사용자에게 반려 알림 전송");
        }
    }
}
