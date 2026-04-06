package com.kimdoolim.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private final Long reservationId;
    private final Long periodId;
    private final Long userId;
    private final Long facilityId;
    private final Long equipmentId;
    private final String purpose;
    private final LocalDateTime createdAt;
    private final LocalDate reservationDate;
    private final String status;
    private final String realUse;
    private final LocalDateTime approvedAt;
    private final String targetType;

    // Builder를 통해서만 생성 가능하도록 private 생성자 설정
    private Reservation(Builder builder) {
        this.reservationId = builder.reservationId;
        this.periodId = builder.periodId;
        this.userId = builder.userId;
        this.facilityId = builder.facilityId;
        this.equipmentId = builder.equipmentId;
        this.purpose = builder.purpose;
        this.createdAt = builder.createdAt;
        this.reservationDate = builder.reservationDate;
        this.status = builder.status;
        this.realUse = builder.realUse;
        this.approvedAt = builder.approvedAt;
        this.targetType = builder.targetType;
    }

    // Static Inner Builder Class
    public static class Builder {
        private Long reservationId;
        private Long periodId;
        private Long userId;
        private Long facilityId;
        private Long equipmentId;
        private String purpose;
        private LocalDateTime createdAt;
        private LocalDate reservationDate;
        private String status;
        private String realUse;
        private LocalDateTime approvedAt;
        private String targetType;

        public Builder reservationId(Long reservationId) { this.reservationId = reservationId; return this; }
        public Builder periodId(Long periodId) { this.periodId = periodId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder facilityId(Long facilityId) { this.facilityId = facilityId; return this; }
        public Builder equipmentId(Long equipmentId) { this.equipmentId = equipmentId; return this; }
        public Builder purpose(String purpose) { this.purpose = purpose; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder reservationDate(LocalDate reservationDate) { this.reservationDate = reservationDate; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder realUse(String realUse) { this.realUse = realUse; return this; }
        public Builder approvedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; return this; }
        public Builder targetType(String targetType) { this.targetType = targetType; return this; }

        public Reservation build() {
            return new Reservation(this);
        }
    }

    // Getters (Setter는 빌더 패턴의 불변성을 위해 제외하거나 필요한 경우 추가)
    public Long getReservationId() { return reservationId; }
    public Long getPeriodId() { return periodId; }
    public Long getUserId() { return userId; }
    public Long getFacilityId() { return facilityId; }
    public Long getEquipmentId() { return equipmentId; }
    public String getPurpose() { return purpose; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDate getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
    public String getRealUse() { return realUse; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public String getTargetType() { return targetType; }

    // equals & hashCode: 주로 PK인 reservationId를 기준으로 비교
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    // toString: 디버깅 시 필드 확인용
    @Override
    public String toString() {
        return "ReservationDto{" +
                "reservationId=" + reservationId +
                ", periodId=" + periodId +
                ", userId=" + userId +
                ", facilityId=" + facilityId +
                ", equipmentId=" + equipmentId +
                ", purpose='" + purpose + '\'' +
                ", createdAt=" + createdAt +
                ", reservationDate=" + reservationDate +
                ", status='" + status + '\'' +
                ", realUse='" + realUse + '\'' +
                ", approvedAt=" + approvedAt +
                ", targetType='" + targetType + '\'' +
                '}';
    }
}
