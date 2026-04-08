package com.kimdoolim.alarm;

import com.kimdoolim.dto.Alarm;
import com.kimdoolim.main.ClientMain;

import java.time.format.DateTimeFormatter;

public class AlarmSendingManager {
    //알림 [타입] 시설예약 요청이 왔습니다 (2025-04-01 22:10)

    // :으로 구분하기때문에 문자열에 :이 포함되어있으면 안됩니다.
    // 타입은 반납요청/예약요청/강제취소/반납/사용/요청결과/연체
    public void sendingAlarm(Alarm alarm) {
        String fullContent = "";
        String type = "[" + alarm.getType() + "]";
        String content = alarm.getContent();

        if(content.contains(":")) {
            System.out.println("alarmMessage에는 :이 포함되면 안됩니다.");
            return;
        }

        String date = alarm.getGenerateDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        fullContent = alarm.getReceiverId() + ":알림 " + type + " " + content + " (" + date + ")";

        ClientMain.out.println(fullContent);
    }
}
