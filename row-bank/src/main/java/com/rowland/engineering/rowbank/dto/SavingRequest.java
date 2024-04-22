package com.rowland.engineering.rowbank.dto;


import com.rowland.engineering.rowbank.model.SavingType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
public class SavingRequest {

    @NotBlank
    @Min(value = 100)
    private BigDecimal amount;

    @NotBlank
    private String description;

    @NotNull
    private SavingType savingType;
    private LocalDate maturityDate;
}
