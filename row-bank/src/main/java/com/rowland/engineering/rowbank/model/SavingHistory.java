package com.rowland.engineering.rowbank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SavingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private SavingHistoryTransactionType type;

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "savings_id")
    private Saving savings;
}
