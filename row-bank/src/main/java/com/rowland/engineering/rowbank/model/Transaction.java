package com.rowland.engineering.rowbank.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@NoArgsConstructor
@Data
@Entity
@AllArgsConstructor
@Builder
@Table(name = "transactions_table")
@Validated
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private BankName bankName;


    @Min(value = 50)
    private BigDecimal amount;
    @PastOrPresent
    private LocalDateTime timestamp;

    @Size(max = 300)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Valid
    private User user;

}
