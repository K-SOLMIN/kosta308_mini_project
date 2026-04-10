package com.kimdoolim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    private long reservationId;
    private Period period;
    private User user;
    private Facility facility;
    private Equipment equipment;
    private String purpose;
    private LocalDateTime createdAt;
    private LocalDate reservationDate;
    private String status;
    private String reason;
    private String realUse;
    private LocalDateTime approvedAt;
    private String targetType;
    private LocalDateTime returnedAt;   // 실제 반납 시각 (return_request.created_at)
}