package com.rowland.engineering.rowbank.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MakeDeposit {

    @PositiveOrZero
    private BigDecimal depositAmount;

    @Size(max = 300)
    private String description;
}
