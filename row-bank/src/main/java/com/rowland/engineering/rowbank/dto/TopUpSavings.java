package com.rowland.engineering.rowbank.dto;

import com.rowland.engineering.rowbank.model.SavingType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class TopUpSavings {

    @NotNull
    private Long savingId;
    @NotBlank
    @Min(value = 100)
    private BigDecimal amount;

    @NotBlank
    private String description;

    @NotNull
    private SavingType savingType;

}
