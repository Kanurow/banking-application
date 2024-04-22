package com.rowland.engineering.rowbank.dto;

import com.rowland.engineering.rowbank.model.SavingType;
import com.rowland.engineering.rowbank.model.User;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingResponse {

    private BigDecimal amount;

    private int durationInMonths;

    private float interestRate;
    @FutureOrPresent
    private LocalDateTime startDate;
    @NotBlank
    private BigDecimal accruedInterest;
    private boolean matured;
    @Future
    private LocalDate endDate;

    private String description;

    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SavingType savingType;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @Valid
    private User user;
}
