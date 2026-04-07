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
    private static final AlarmScheduler instance = new AlarmScheduler();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    // [수정] ReservationService 제거, AlarmService만 사용
    private final AlarmService alarmService = new AlarmService();

    private AlarmScheduler() {}
    public static AlarmScheduler getInstance() { return instance; }

    /**
     * 알림 시스템 가동 (테스트용으로 즉시 스캔 시작되게 설정 가능)
     */
    public void startSchedule() {
        LocalDateTime now = LocalDateTime.now();
        // 테스트용: 현재 시간에서 5초 뒤에 당일 예약 전체 스캔 시작
        LocalDateTime targetTime = now.plusSeconds(5);

        long initialDelay = Duration.between(now, targetTime).getSeconds();
        scheduler.schedule(this::processDailyNotifications, initialDelay, TimeUnit.SECONDS);

        System.out.println("📢 [알림 시스템] 스캐너 대기 중... (잠시 후 데이터 조회를 시작합니다)");
    }

    /**
     * 실시간 예약 승인 시 호출용 메서드
     */
    public void addReservationAlarm(Reservation reservation) {
        if (reservation == null || !reservation.getReservationDate().equals(LocalDate.now())) return;

        System.out.println("🆕 [실시간 등록] 당일 예약 감지: ID " + reservation.getReservationId());
        scheduleTask(reservation, "START", reservation.getPeriod().getStartTime());
        scheduleTask(reservation, "RETURN", reservation.getPeriod().getEndTime());
    }

    private void processDailyNotifications() {
        System.out.println("🔍 [데이터 조회] AlarmService를 통해 오늘자 예약을 직접 조회합니다...");

        // [수정] AlarmService에 구현할 조회 메서드 호출
        List<Reservation> todayReservations = alarmService.getTodayApprovedReservations();

        if (todayReservations.isEmpty()) {
            System.out.println("❌ 조회된 예약 데이터가 없습니다.");
            return;
        }

        for (Reservation res : todayReservations) {
            addReservationAlarm(res);
        }
    }

    private void scheduleTask(Reservation reservation, String type, LocalTime targetLocalTime) {
        LocalDateTime alarmTime = LocalDateTime.of(LocalDate.now(), targetLocalTime).minusMinutes(10);
        long delay = Duration.between(LocalDateTime.now(), alarmTime).getSeconds();

        // 테스트 시 이미 지난 시간은 3초 뒤에 바로 실행되게 처리
        if (delay < 0) delay = 3;

        scheduler.schedule(() -> {
            String resourceName = "FACILITY".equals(reservation.getTargetType())
                    ? reservation.getFacility().getName()
                    : reservation.getEquipment().getName();

            String message = (type.equals("START"))
                    ? String.format("🔔 [사용 안내] '%s' 사용 10분 전입니다.", resourceName)
                    : String.format("🔔 [반납 안내] '%s' 반납 10분 전입니다.", resourceName);

            // DB 저장 및 소켓 발송
            alarmService.sendAndSaveAlarm(reservation.getUser().getUserId(), message, type);

            System.out.println("🔔 [" + type + " 발송] " + reservation.getUser().getName() + "님께 전송 완료");
        }, delay, TimeUnit.SECONDS);
    }
}