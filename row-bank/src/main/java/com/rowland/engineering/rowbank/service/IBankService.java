package com.rowland.engineering.rowbank.service;

import com.rowland.engineering.rowbank.dto.*;
import com.rowland.engineering.rowbank.model.Saving;
import com.rowland.engineering.rowbank.security.UserPrincipal;

import java.util.List;

public interface IBankService {
    BeneficiaryResponse getBeneficiaryDetails(BeneficiaryRequest beneficiaryRequest);
    TransferResponse makeTransfer(MakeTransfer makeTransfer, UserPrincipal currentUser);
    SavingResponse createFlexibleSavingPlan(SavingRequest savingRequest, UserPrincipal currentUser);
    SavingResponse topUpFlexibleSavingPlan(TopUpSavings savingRequest, UserPrincipal currentUser);
    SavingResponse withdrawFromFlexibleSaving(WithdrawFromSaving withdrawFromSaving, UserPrincipal currentUser);
    boolean deleteFlexibleSaving(Long id, UserPrincipal currentUser);
    SavingResponse createFixedSavingPlan(SavingRequest savingRequest, UserPrincipal currentUser);
    List<Saving> getAllFixedSavings();
    List<Saving> getAllFlexibleSavings();
}
