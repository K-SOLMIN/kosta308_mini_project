package com.kimdoolim.dto;

import java.time.LocalTime;
import java.util.Objects;

public class Period {
    private final int periodId;
    private final String periodName;
    private final LocalTime startTime;
    private final LocalTime endTime;

    // 빌더를 통한 생성을 위해 private 생성자 사용
    private Period(Builder builder) {
        this.periodId = builder.periodId;
        this.periodName = builder.periodName;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    }

    public int getPeriodId() {
        return periodId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    // Builder 클래스
    public static class Builder {
        private int periodId;
        private String periodName;
        private LocalTime startTime;
        private LocalTime endTime;

        public Builder periodId(int periodId) { this.periodId = periodId; return this; }
        public Builder periodName(String periodName) { this.periodName = periodName; return this; }
        public Builder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public Builder endTime(LocalTime endTime) { this.endTime = endTime; return this; }

        public Period build() {
            return new Period(this);
        }
    }

    // toString 오버라이딩
    @Override
    public String toString() {
        return "PeriodDto{" +
                "periodId=" + periodId +
                ", periodName='" + periodName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    // equals & hashCode 오버라이딩
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Period period = (Period) o;
        return Objects.equals(periodId, period.periodId) &&
                Objects.equals(periodName, period.periodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodId, periodName);
    }
}
