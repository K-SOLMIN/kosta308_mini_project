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
        LocalDateTime todaySix = now.toLocalDate().atTime(6, 0);
        LocalDateTime nextSix = now.isAfter(todaySix) ? todaySix.plusDays(1) : todaySix;

        // 오전 6시 이후에 서버가 시작됐으면 2초 뒤 즉시 조회
        if (now.isAfter(todaySix)) {
            scheduler.schedule(this::getTodayApprovedReservation, 2, TimeUnit.SECONDS);
            System.out.println("📢 [알림 시스템] 오전 6시 이후 서버 시작 감지 → 2초 뒤 즉시 조회 실행");
        }

        long initialDelay = Duration.between(now, nextSix).getSeconds();
        scheduler.scheduleAtFixedRate(
                this::getTodayApprovedReservation,
                initialDelay,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        System.out.println("📢 [알림 시스템] 매일 오전 6시에 알림 조회를 시작합니다. 다음 실행: " + nextSix);
    }

    public void addReservationAlarm(Reservation reservation) {
        if (reservation == null || !reservation.getReservationDate().equals(LocalDate.now())) return;

        // 중복 등록 방지: 이미 등록된 스케줄이 있으면 먼저 취소
        if (scheduledTasks.containsKey(reservation.getReservationId())) {
            cancelReservationAlarm(reservation.getReservationId());
        }

        System.out.println("🆕 [실시간 등록] 당일 예약 감지: ID " + reservation.getReservationId());

        List<ScheduledFuture<?>> futures = new ArrayList<>();
        LocalTime now = LocalTime.now();
        LocalTime startTime = reservation.getPeriod().getStartTime();
        LocalTime endTime = reservation.getPeriod().getEndTime();

        String resourceName = (reservation.getFacility() != null)
                ? reservation.getFacility().getName()
                : reservation.getEquipment().getName();

        // START 알림: startTime이 아직 안 지난 경우만 등록
        if (now.isBefore(startTime)) {
            if (now.isAfter(startTime.minusMinutes(10))) {
                // 10분 이내 → 남은 분 표시하여 즉시 발송
                long minutesLeft = Duration.between(now, startTime).toMinutes();
                String msg = String.format("🔔 [사용 안내] '%s' 사용 %d분 전입니다.", resourceName, minutesLeft);
                alarmService.sendAndSaveAlarm(reservation.getUser().getUserId(), msg, "사용안내");
                System.out.println("🔔 [즉시 사용안내 발송] " + reservation.getUser().getName() + "님께 전송 완료");
            } else {
                futures.add(scheduleTask(reservation, "START", startTime));
            }
        }

        // RETURN 알림: endTime - 10분이 아직 안 지난 경우만 등록
        if (now.isBefore(endTime.minusMinutes(10))) {
            futures.add(scheduleTask(reservation, "RETURN", endTime));
        } else if (now.isBefore(endTime)) {
            // 10분 이내 → 남은 분 표시하여 즉시 발송
            long minutesLeft = Duration.between(now, endTime).toMinutes();
            String msg = String.format("🔔 [반납안내] '%s' 예약 종료 %d분 전입니다. 종료 이후 10분 이내에 반납해주세요.", resourceName, minutesLeft);
            alarmService.sendAndSaveAlarm(reservation.getUser().getUserId(), msg, "반납안내");
            System.out.println("🔔 [즉시 반납안내 발송] " + reservation.getUser().getName() + "님께 전송 완료");
        }

        // 연체 알림: endTime + 2분이 아직 안 지난 경우만 등록
        if (now.isBefore(endTime.plusMinutes(2))) {
            futures.add(scheduleOverdueAlarm(reservation));
        }

        if (!futures.isEmpty()) {
            scheduledTasks.put(reservation.getReservationId(), futures);
        }
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
        System.out.println("delay : " + delay + " / type: " + type);
        if (delay < 0) delay = 3;

        return scheduler.schedule(() -> {
            try {
                String resourceName = (reservation.getFacility() != null)
                        ? reservation.getFacility().getName()
                        : reservation.getEquipment().getName();

                long actualMinutesLeft = Duration.between(LocalTime.now(), targetLocalTime).toMinutes();
                String message = (type.equals("START"))
                        ? String.format("🔔 [사용안내] '%s' 사용시작 %d분 전입니다.", resourceName, actualMinutesLeft)
                        : String.format("🔔 [반납안내] '%s' 예약 종료 %d분 전입니다. 종료 이후 10분 이내에 반납해주세요.", resourceName, actualMinutesLeft);

                String alarmType = type.equals("START") ? "사용안내" : "반납안내";
                alarmService.sendAndSaveAlarm(reservation.getUser().getUserId(), message, alarmType);
                System.out.println("🔔 [" + type + " 발송] " + reservation.getUser().getName() + "님께 전송 완료");
            } catch (Exception e) {
                System.err.println("❌ [" + type + " 알람 발송 실패] 예약 ID: " + reservation.getReservationId() + " / 원인: " + e.getMessage());
                e.printStackTrace();
            }
        }, delay, TimeUnit.SECONDS);
    }

    // endTime에 반납 여부 확인 후 연체알림 발송
    private ScheduledFuture<?> scheduleOverdueAlarm(Reservation reservation) {
        // endTime + 10분에 실행
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), reservation.getPeriod().getEndTime())
                .plusMinutes(10);
        long delay = Duration.between(LocalDateTime.now(), endDateTime).getSeconds();
        if (delay < 0) delay = 3;

        System.out.println("futureDelay : " + delay);

        return scheduler.schedule(() -> {
            try {
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
            } catch (Exception e) {
                System.err.println("❌ [연체 알람 발송 실패] 예약 ID: " + reservation.getReservationId() + " / 원인: " + e.getMessage());
                e.printStackTrace();
            }
        }, delay, TimeUnit.SECONDS);
    }
}