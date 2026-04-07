package com.kimdoolim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facility {
    private long facilityId;             // PK, Auto_Increment
    private User user;              // FK, 시설담당자
    private String location;             // 위치
    private String name;                 // 시설이름
    private int maxCapacity;             // 최대 수용인원
    private String maxReservationUnit;   // 시설 최대예약가능 단위
    private int maxReservationValue;    // 시설 최대예약가능 값
    private boolean isDelete;            // 시설 삭제 여부 (TINYINT(1) 매핑 권장)
    private LocalDateTime deleteDate;    // 시설 삭제 날짜
    private String status;               // 상태 (정상, 수리, 점검 등)
}