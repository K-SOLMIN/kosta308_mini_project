package com.kimdoolim.alarm;

import com.kimdoolim.dto.Reservation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class AlarmScheduler {
    private static final AlarmScheduler alarmScheduler = new AlarmScheduler();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final AlarmService alarmService = AlarmService.getAlarmService();
    private final Map<Long, List<ScheduledFuture<?>>> scheduledTasks = new ConcurrentHashMap<>();

    private AlarmScheduler() {}
    public static AlarmScheduler getAlarmScheduler() { return alarmScheduler; }

    public void startScheduleTest() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.plusSeconds(5);
        long initialDelay = Duration.between(now, targetTime).getSeconds();
        scheduler.schedule(this::getTodayApprovedReservation, initialDelay, TimeUnit.SECONDS);
        System.out.println("📢 [알림 시스템] 스캐너 대기 중...");
    }

    public void startSchedule() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.toLocalDate().atTime(8, 0);
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1);
        }
        long initialDelay = Duration.between(now, targetTime).getSeconds();
        scheduler.scheduleAtFixedRate(
                this::getTodayApprovedReservation,
                initialDelay,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        System.out.println("📢 [알림 시스템] 매일 오전 8시에 알림 조회를 시작합니다. 다음 실행: " + targetTime);
    }

    public void addReservationAlarm(Reservation reservation) {
        if (reservation == null || !reservation.getReservationDate().equals(LocalDate.now())) return;

        System.out.println("🆕 [실시간 등록] 당일 예약 감지: ID " + reservation.getReservationId());

        List<ScheduledFuture<?>> futures = new ArrayList<>();
        futures.add(scheduleTask(reservation, "START", reservation.getPeriod().getStartTime()));
        futures.add(scheduleTask(reservation, "RETURN", reservation.getPeriod().getEndTime()));
        futures.add(scheduleOverdueAlarm(reservation));  // 연체알림 동시 등록

        scheduledTasks.put(reservation.getReservationId(), futures);
    }

    public void cancelReservationAlarm(long reservationId) {
        List<ScheduledFuture<?>> futures = scheduledTasks.remove(reservationId);
        if (futures != null) {
            futures.forEach(f -> f.cancel(false));  // 사용 + 반납 + 연체 전부 취소
            System.out.println("❌ [스케줄 전체 취소 완료] 예약 ID: " + reservationId);
        } else {
            System.out.println("⚠️ [스케줄 취소 실패] 등록된 스케줄 없음 예약 ID: " + reservationId);
        }
    }

    private void getTodayApprovedReservation() {
        System.out.println("🔍 [데이터 조회] 오늘자 승인된 알람 조회");
        List<Reservation> todayReservations = alarmService.getTodayApprovedReservations();

        if (todayReservations.isEmpty()) {
            System.out.println("❌ 조회된 예약 데이터가 없습니다.");
            return;
        }
        for (Reservation res : todayReservations) {
            addReservationAlarm(res);
        }
    }

    private ScheduledFuture<?> scheduleTask(Reservation reservation, String type, LocalTime targetLocalTime) {
        LocalDateTime alarmTime = LocalDateTime.of(LocalDate.now(), targetLocalTime).minusMinutes(10);
        long delay = Duration.between(LocalDateTime.now(), alarmTime).getSeconds();
        if (delay < 0) delay = 3;

        return scheduler.schedule(() -> {
            String resourceName = (reservation.getFacility() != null)
                    ? reservation.getFacility().getName()
                    : reservation.getEquipment().getName();

            String message = (type.equals("START"))
                    ? String.format("🔔 [사용안내] '%s' 사용 10분 전입니다.", resourceName)
                    : String.format("🔔 [반납안내] '%s' 반납 10분 전입니다.", resourceName);

            alarmService.sendAndSaveAlarm(reservation.getUser().getUserId(), message, type);
            System.out.println("🔔 [" + type + " 발송] " + reservation.getUser().getName() + "님께 전송 완료");
        }, delay, TimeUnit.SECONDS);
    }

    // endTime에 반납 여부 확인 후 연체알림 발송
    private ScheduledFuture<?> scheduleOverdueAlarm(Reservation reservation) {
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), reservation.getPeriod().getEndTime());
        long delay = Duration.between(LocalDateTime.now(), endDateTime).getSeconds();
        if (delay < 0) delay = 3;

        return scheduler.schedule(() -> {
            // 실행 시점에 반납 여부 DB 조회
            boolean isReturned = alarmService.isAlreadyReturned(reservation.getReservationId());

            if (isReturned) {
                System.out.println("✅ [연체알림 스킵] 예약 ID: " + reservation.getReservationId() + " 이미 반납 완료");
                return;
            }

            String resourceName = (reservation.getFacility() != null)
                    ? reservation.getFacility().getName()
                    : reservation.getEquipment().getName();

            String msg = "⚠️ [연체안내] '" + resourceName + "' 반납 시간이 지났습니다. 즉시 반납해주세요.";
            alarmService.sendAndSaveAlarm(reservation.getUser().getUserId(), msg, "연체안내");

            System.out.println("⚠️ [연체알림 발송] " + reservation.getUser().getName() + "님께 전송 완료");
        }, delay, TimeUnit.SECONDS);
    }
}