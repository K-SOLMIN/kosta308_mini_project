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
public class BlockPeriod {
    private int blockPeriodId;
    private LocalDate startDate; // int -> LocalDate 권장
    private LocalDate endDate;   // int -> LocalDate 권장
    private String description;
}
