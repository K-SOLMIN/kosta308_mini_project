package com.kimdoolim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipmentDetail {
    private long equipmentDetailId; // PK, Auto_Increment
    private long equipmentId;       // FK, 비품아이디
    private boolean checkDelete;    // 삭제여부 (TINYINT(1) 매핑)
    private LocalDate deleteDate;   // 삭제 처리 날짜 (DATE)
    private String serialNo;        // 시리얼번호
    private String status;          // 상태 (정상, 수리, 점검 등)
}
