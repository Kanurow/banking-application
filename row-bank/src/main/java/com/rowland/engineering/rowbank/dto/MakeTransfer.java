package com.rowland.engineering.rowbank.dto;

import com.rowland.engineering.rowbank.model.BankName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeTransfer {

    private BigDecimal transferAmount;
    private String receiverFirstName;
    private String receiverLastName;
    private String description;
    private String accountNumberOrEmail;
    private BankName bankToBeCredited;
    private BankName bankToBeDebited;

}
