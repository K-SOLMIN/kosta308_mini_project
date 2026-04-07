package com.kimdoolim.alarm;

import com.kimdoolim.dto.Reservation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlarmScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final AlarmService service = new AlarmService();

    public void startTestSchedule() {
        // [1] 테스트 실행 시간 설정: 오늘 밤 10시 45분 (22:45)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.withHour(22).withMinute(45).withSecond(0).withNano(0);

        // 만약 이미 10시 45분이 지났다면 테스트를 위해 1분 뒤로 설정하거나 내일로 넘김
        if (now.isAfter(targetTime)) {
            System.out.println("⚠️ 설정한 22:45분이 이미 지났습니다. 테스트를 위해 현재 시간 기준 10초 뒤로 재설정합니다.");
            targetTime = now.plusSeconds(10);
        }

        long initialDelay = Duration.between(now, targetTime).getSeconds();

        // [2] 지정된 시간에 1회 실행 (테스트용이므로 반복 주기 생략 가능하지만 구조 유지)
        scheduler.schedule(this::scheduleDailyAlarms, initialDelay, TimeUnit.SECONDS);

        System.out.println("🚀 [테스트 시작] " + targetTime + "에 예약 스캔이 시작됩니다. (남은 시간: " + initialDelay + "초)");
    }

    private void scheduleDailyAlarms() {
        System.out.println("\n[🔔 스캐너 작동] 금일 예약 목록을 조회합니다...");

        //오늘 날짜의 예약 가져오기 (DB 연동 확인 필요)
        List<Reservation> todayReservations = service.getReservationsByDate(LocalDate.now());

        if (todayReservations.isEmpty()) {
            System.out.println("❌ 오늘 예약된 건이 없습니다. 테스트 데이터(DB)를 확인해주세요.");
            return;
        }

        for (Reservation reservation : todayReservations) {
            scheduleReturnReminder(reservation);
        }
    }

    private void scheduleReturnReminder(Reservation reservation) {
        // [3] 종료 시간 10분 전 알림 예약 로직
        LocalTime endTime = reservation.getPeriod().getEndTime();
        LocalDateTime alarmTarget = LocalDateTime.of(LocalDate.now(), endTime).minusMinutes(10);

        long delay = Duration.between(LocalDateTime.now(), alarmTarget).getSeconds();

        // 테스트 편의상: 만약 종료 10분 전이 이미 지났다면, 5초 뒤에 바로 알림이 오도록 설정
        if (delay < 0) {
            System.out.println("⚠️ ID " + reservation.getReservationId() + "번은 이미 알림 시간이 지났습니다. 테스트를 위해 5초 뒤 즉시 발송합니다.");
            delay = 5;
        }

        scheduler.schedule(() -> {
            sendSocketMessage(reservation);
        }, delay, TimeUnit.SECONDS);

        System.out.println("✅ [알림 예약 완료] 예약ID: " + reservation.getReservationId() + " / 발송 예정시간: " + alarmTarget + " (약 " + delay + "초 후)");
    }

    private void sendSocketMessage(Reservation reservation) {
        // 자원 명칭 추출
        String resourceName = reservation.getTargetType().equals("FACILITY")
                ? (reservation.getFacility() != null ? reservation.getFacility().getName() : "Unknown Facility")
                : (reservation.getEquipment() != null ? reservation.getEquipment().getName() : "Unknown Equipment");

        System.out.println("\n[📢 소켓 메시지 전송 시뮬레이션]");
        System.out.println("내용: 🔔 [반납 알림] '" + resourceName + "' 반납 시간 10분 전입니다. 정해진 시간 내에 반납해주세요.");
        // 여기에 실제 소켓 전송 메서드 연결 (예: userSession.send(msg))
    }
}