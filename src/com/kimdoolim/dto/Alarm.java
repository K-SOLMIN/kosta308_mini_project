package com.kimdoolim.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alarm {

    // 알람번호: ALARM_ID (PK, AUTO_INCREMENT, INT)
    private int alarmId;

    // 알람 수신자: RECEIVER_ID (FK, NOT NULL, INT)
    private int receiverId;

    // 알람타입: TYPE (NOT NULL, VARCHAR(10))
    // 예: 'START', 'RETURN' 등
    private String type;

    // 알람생성날짜: GENERATE_DATE (NOT NULL, DEFAULT SYSDATE, DATETIME)
    private LocalDateTime generateDate;

    // 알람내용: CONTENT (NOT NULL, VARCHAR(100))
    private String content;

    // 읽음여부: ISREAD (NOT NULL, CHECK(TRUE, FALSE), VARCHAR(10))
    // 이미지에 VARCHAR(10)으로 되어 있어 String으로 처리합니다.
    private String isRead;

    // 읽은날짜: READDATE (Domain, DATE)
    // 읽기 전에는 NULL일 수 있으므로 객체 타입인 LocalDate를 사용합니다.
    private LocalDate readDate;

}