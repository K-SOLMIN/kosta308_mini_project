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
    private final Map<Long, List<ScheduledFuture<?>>> scheduledTasks = new ConcurrentHashMap<>(); //key : reservationId

    private AlarmScheduler() {}
    public static AlarmScheduler getAlarmScheduler() { return alarmScheduler; }

    //테스트용
    public void startScheduleTest() {
        LocalDateTime now = LocalDateTime.now();
        // 테스트용: 현재 시간에서 5초 뒤에 당일 예약 전체 스캔 시작
        LocalDateTime targetTime = now.plusSeconds(5);

        long initialDelay = Duration.between(now, targetTime).getSeconds();
        scheduler.schedule(this::getTodayApprovedReservation, initialDelay, TimeUnit.SECONDS);

        System.out.println("📢 [알림 시스템] 스캐너 대기 중... (잠시 후 데이터 조회를 시작합니다)");
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

    /**
     * 승인된 예약 scheduleTask메소드 호출해서 시작시간 반납시간 맞춰서 알림발송
     */
    public void addReservationAlarm(Reservation reservation) {
        if (reservation == null || !reservation.getReservationDate().equals(LocalDate.now())) return;

        List<ScheduledFuture<?>> futures = new ArrayList<>();
        System.out.println("🆕 [실시간 등록] 당일 예약 감지: ID " + reservation.getReservationId());
        futures.add(scheduleTask(reservation, "START_RESERVATION", reservation.getPeriod().getStartTime()));
        futures.add(scheduleTask(reservation, "END_RESERVATION", reservation.getPeriod().getEndTime()));

        // 예약 ID로 task 저장
        scheduledTasks.put(reservation.getReservationId(), futures);
    }

    public void cancelReservationAlarm(long reservationId) {
        List<ScheduledFuture<?>> futures = scheduledTasks.remove(reservationId);
        if (futures != null) {
            futures.forEach(f -> f.cancel(false));
            System.out.println("❌ [스케줄 취소 완료] 예약 ID: " + reservationId);
        } else {
            System.out.println("⚠️ [스케줄 취소 실패] 등록된 스케줄 없음 예약 ID: " + reservationId);
        }
    }

    private void getTodayApprovedReservation() {
        System.out.println("🔍 [데이터 조회] 오늘자 승인된 알람 조회");

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

    public void scheduleOverdueAlarm(long reservationId) {
        // 1. 해당 예약 정보 가져오기 (종료 시간 확인용)
        Reservation res = alarmService.getReservationById(reservationId);
        if (res == null) return;

        // 2. 종료 시간 + 10분 계산
        //테스트로 endTime + 1분뒤에 보내는중
        LocalDateTime overdueCheckTime = LocalDateTime.of(LocalDate.now(), res.getPeriod().getEndTime()).plusMinutes(1);
        long delay = Duration.between(LocalDateTime.now(), overdueCheckTime).getSeconds();

        if (delay < 0) delay = 3; // 이미 지난 경우 즉시(3초 뒤) 체크(테스트용)

        // 3. 스케줄 등록
        scheduler.schedule(() -> {
            // [중요] 10분 뒤 시점에 다시 DB 조회 (반납 여부 확인)
            boolean isReturned = alarmService.isAlreadyReturned(reservationId);

            if (!isReturned) {
                String message = "🔔 [연체 알림] 다음 사용자를 위해 빠른 반납 부탁드립니다.";
                // 수신자에게 발송
                alarmService.sendAndSaveAlarm(res.getUser().getUserId(), message, "OVERDUE");
                System.out.println("🚨 [연체 발송] ID " + reservationId + " 유저에게 연체 경고 전송");
            } else {
                System.out.println("✅ [연체 제외] ID " + reservationId + " 정상 반납 확인됨");
            }
        }, delay, TimeUnit.SECONDS);
    }

    private ScheduledFuture<?> scheduleTask(Reservation reservation, String type, LocalTime targetLocalTime) {
        LocalDateTime alarmTime = LocalDateTime.of(LocalDate.now(), targetLocalTime).minusMinutes(10);
        long delay = Duration.between(LocalDateTime.now(), alarmTime).getSeconds();
        System.out.println("delay : " + delay);
        // 테스트 시 이미 지난 시간은 3초 뒤에 바로 실행되게 처리
        if (delay < 0) {
            System.out.println("이미지난 시간임");
            delay = 3;
        }

        return scheduler.schedule(() -> {
            String resourceName = "FACILITY".equals(reservation.getTargetType())
                    ? reservation.getFacility().getName()
                    : reservation.getEquipment().getName();

            String message = (type.equals("START_RESERVATION"))
                    ? String.format("🔔 [사용 안내] '%s' 사용 10분 전입니다.", resourceName)
                    : String.format("🔔 [반납 안내] '%s' 반납 10분 전입니다.", resourceName);

            // DB 저장 및 소켓 발송
            alarmService.sendAndSaveAlarm(reservation.getUser().getUserId(), message, type);

            System.out.println("🔔 [" + type + " 발송] " + reservation.getUser().getName() + "님께 전송 완료");
        }, delay, TimeUnit.SECONDS);
    }
}