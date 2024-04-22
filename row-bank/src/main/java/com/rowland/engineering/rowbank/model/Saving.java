package com.rowland.engineering.rowbank.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
@AllArgsConstructor
@Builder
@Table(name = "savings_table")
public class Saving {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 100)
    private BigDecimal amount;

    @NotNull
    private BigDecimal interestRate;

    private BigDecimal interestEarned;

    private LocalDate startDate;

    @NotBlank
    private String description;


    private Boolean isActive;
    private LocalDate maturityDate;


    @NotNull
    @Enumerated(EnumType.STRING)
    private SavingType savingType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

//    @OneToMany(mappedBy = "savings", cascade = CascadeType.ALL)
//    private List<SavingHistory> savingHistories = new ArrayList<>();
}
