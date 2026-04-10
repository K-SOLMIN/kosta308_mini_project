package com.kimdoolim.view;

import com.kimdoolim.alarm.AlarmService;
import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Alarm;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AlarmView {

    private static final AlarmService alarmService = AlarmService.getAlarmService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void alarmMenu() {
        AppScanner.cls();
        int userId = Auth.getUserInfo().getUserId();

        // 읽음 처리 전에 미읽 알림 먼저 조회
        List<Alarm> alarms = alarmService.getMyAlarms(userId);
        alarmService.markAllAlarmsAsRead(userId);

        System.out.println("\n[ 알림 목록 ]");
        System.out.println("─".repeat(50));

        if (alarms.isEmpty()) {
            System.out.println("  읽지 않은 알림이 없습니다.");
        } else {
            for (Alarm alarm : alarms) {
                String date = alarm.getGenerateDate().format(FMT);
                System.out.printf("  %s  (%s)%n", alarm.getContent(), date);
            }
        }

        System.out.println("─".repeat(50));
        System.out.println(" 0. 뒤로가기");
        System.out.print("선택: ");
        AppScanner.getScanner().nextLine();
    }
}
