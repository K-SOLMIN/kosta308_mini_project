package com.kimdoolim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockPeriod {
    private int blockPeriodId;
    private int startDate;
    private int endDate;
    private String description;
}
