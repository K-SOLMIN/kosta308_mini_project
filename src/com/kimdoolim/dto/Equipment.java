package com.kimdoolim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {
    private long equipmentId;      // PK, Auto_Increment
    private User user;        // FK, 비품관리자 아이디
    private String name;           // 비품명
    private String location;       // 위치
    private boolean checkDelete;   // 비품삭제여부 (TINYINT(1) 매핑)
    private LocalDate deletedate;  // 비품삭제날짜 (DATE)
    private String serialNo;       // 시리얼 기본값(1개) 또는 자동생성 접두사(여러개)
    private String status;         // 상태 (정상, 수리, 점검 등)
    private int quantity;          // 낱개 수량 (equipment_detail COUNT)
    private String statusSummary;  // 낱개별 상태 요약 (예: "9 정상 / 1 고장")
}
