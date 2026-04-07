package com.kimdoolim.alarm;

import com.kimdoolim.dto.Reservation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlarmService {
     //1. 스케줄러 생성 (쓰레드 풀 크기 지정)
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void scheduleOverdueAlarm(Reservation reservation) {
        // 2. 현재 시간과 (종료시간 + 10분) 사이의 차이 계산
        LocalTime limitTime = reservation.getPeriod().getEndTime().plusMinutes(10);
        long delay = Duration.between(LocalDateTime.now(), limitTime).getSeconds();

        if (delay < 0) return; // 이미 시간이 지났다면 예약 안 함

        // 3. 작업 예약 (Runnable, 지연시간, 단위)
        scheduler.schedule(() -> {
            // --- 10분 뒤에 실행될 로직 ---
            //System.out.println("\n🔔 [알림] " + reservation.getItemName() + " 연체 발생!");
            // 여기서 DB 상태를 한 번 더 체크 (그 사이에 반납했을 수도 있으니까)
            // 소켓으로 메시지 전송 로직 실행
            // ---------------------------
        }, delay, TimeUnit.SECONDS);

        System.out.println(delay + "초 뒤에 알림이 예약되었습니다.");
    }
}