package com.kimdoolim.alarm;

import com.kimdoolim.main.ClientMain;

public class AlarmSendingManager {
    private static final AlarmSendingManager alarmSendingManager = new AlarmSendingManager();

    private AlarmSendingManager() { };

    public static AlarmSendingManager getAlarmSendingManager() {
        return alarmSendingManager;
    }
    //알림 [타입] 시설예약 요청이 왔습니다 (2025-04-01 22:10)

    // :으로 구분하기때문에 문자열에 :이 포함되어있으면 안됩니다.
    // 타입은 반납요청/예약요청/강제취소/반납/사용/요청결과/연체

    //parameter는 receiverId, reservationId 임
    //type은 예약요청/반납요청/취소/반납안내/사용안내/요청결과/연체/사용시작
    public void sendingTextToSocketServer (String type, long parameter) {
        if (ClientMain.out == null) {
            System.out.println("[AlarmSendingManager] 소켓 미연결 - 전송 불가 (type=" + type + ", param=" + parameter + ")");
            return;
        }

        System.out.println("[AlarmSendingManager] 전송 시도: type=" + type + ", param=" + parameter);
        if(type.equals("예약요청")) ClientMain.out.println("REQUEST_RESERVATION:" + parameter); //parameter -> reservationId
        else if(type.equals("취소")) ClientMain.out.println("CANCEL:" + parameter); //parameter -> reservationId
        else if(type.equals("사용시작")) ClientMain.out.println("USE_START:" + parameter); //parameter -> reservationId  (콜론 추가)
        else if(type.equals("요청결과")) ClientMain.out.println("RESERVATION_RESULT:" + parameter); //parameter -> reservationId
        else System.out.println("[AlarmSendingManager] 알 수 없는 타입: " + type);
    }
}
