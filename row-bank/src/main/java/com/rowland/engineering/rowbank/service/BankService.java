package com.rowland.engineering.rowbank.service;

import com.rowland.engineering.rowbank.dto.*;
import com.rowland.engineering.rowbank.exception.*;
import com.rowland.engineering.rowbank.model.*;
import com.rowland.engineering.rowbank.repository.SavingHistoryRepository;
import com.rowland.engineering.rowbank.repository.SavingRepository;
import com.rowland.engineering.rowbank.repository.TransactionRepository;
import com.rowland.engineering.rowbank.repository.UserRepository;
import com.rowland.engineering.rowbank.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BankService implements IBankService{

    @Value("${banking.flexible.interestRate}")
    private double FLEXIBLE_INTEREST_RATE;

    @Value("${banking.fixed.interestRate}")
    private double FIXED_INTEREST_RATE;


    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SavingRepository savingRepository;
    private final SavingHistoryRepository savingHistoryRepository;



    public BeneficiaryResponse getBeneficiaryDetails(BeneficiaryRequest beneficiaryRequest) {
        Optional<User> foundUserAccount = userRepository.findByAccountNumberOrEmail(beneficiaryRequest.getAccountNumberOrEmail(), beneficiaryRequest.getAccountNumberOrEmail());
        if (foundUserAccount.isEmpty()) {
            throw new UserNotFoundException(beneficiaryRequest.getAccountNumberOrEmail() + " not found!!! Check details");
        }
        User beneficiaryAccount = foundUserAccount.get();

        if (beneficiaryAccount.getBankName() != beneficiaryRequest.getBankName()) {
            throw new IncorrectBankNameException(beneficiaryRequest.getAccountNumberOrEmail()
                    + " is not a customer at " + beneficiaryRequest.getBankName());
        }
        BeneficiaryResponse beneficiaryResponse = new BeneficiaryResponse();

        beneficiaryResponse.setId(beneficiaryAccount.getId());
        beneficiaryResponse.setEmail(beneficiaryAccount.getEmail());
        beneficiaryResponse.setFirstName(beneficiaryAccount.getFirstName());
        beneficiaryResponse.setLastName(beneficiaryAccount.getLastName());
        beneficiaryResponse.setAccountNumber(beneficiaryAccount.getAccountNumber());
        beneficiaryResponse.setUsername(beneficiaryAccount.getUsername());
        beneficiaryResponse.setBankName(beneficiaryAccount.getBankName());
        return beneficiaryResponse;
    }


    @Transactional
    public TransferResponse makeTransfer(MakeTransfer makeTransfer, UserPrincipal currentUser) {
        Optional<User> receiver = userRepository.findByAccountNumberOrEmail(makeTransfer.getAccountNumberOrEmail(),
                makeTransfer.getAccountNumberOrEmail());
        Optional<User> sender = userRepository.findById(currentUser.getId());
        if (receiver.isEmpty()) {
            throw new UserNotFoundException("Receiver not found! Check details");
        } else if (sender.isEmpty()) {
            throw new UserNotFoundException("Sender not found! Check details");
        }
        User receiverAccount = receiver.get();
        User senderAccount = sender.get();
        if (makeTransfer.getBankToBeCredited() != receiverAccount.getBankName()) {
            throw new IncorrectBankNameException("Check bank name details!");
        } else if (Objects.equals(currentUser.getId(), receiverAccount.getId())
        ) {
            throw new AccountDetailsMismatch("You cannot transfer into your own account");
        }

        BigDecimal transferAmount = makeTransfer.getTransferAmount();

        if (senderAccount.getBalance().subtract(transferAmount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundException("Insufficient fund to cover transfer");
        }
        Transaction senderTransaction = Transaction.builder()
                .user(senderAccount)
                .description(makeTransfer.getDescription())
                .timestamp(LocalDateTime.now())
                .amount(makeTransfer.getTransferAmount())
                .bankName(senderAccount.getBankName())
                .transactionType(TransactionType.DEBIT)
                .build();
        Transaction receiverTransaction = Transaction.builder()
                .user(receiverAccount)
                .description(makeTransfer.getDescription())
                .timestamp(LocalDateTime.now())
                .amount(makeTransfer.getTransferAmount())
                .bankName(receiverAccount.getBankName())
                .transactionType(TransactionType.CREDIT)
                .build();

        transactionRepository.save(senderTransaction);
        transactionRepository.save(receiverTransaction);

        senderAccount.setBalance(senderAccount.getBalance().subtract(transferAmount));
        receiverAccount.setBalance(receiverAccount.getBalance().add(transferAmount));
        userRepository.save(senderAccount);
        userRepository.save(receiverAccount);
        return TransferResponse.builder()
                .amount(makeTransfer.getTransferAmount())
                .remainingSenderBalance(senderAccount.getBalance())
                .receiverFullName(receiverAccount.getFirstName() + " " + receiverAccount.getLastName())
                .build();
    }

    @Transactional
    public SavingResponse createFlexibleSavingPlan(SavingRequest savingRequest, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + currentUser.getId()));
        if (savingRequest.getAmount() == null) {
            throw new IllegalArgumentException("Invalid input: All fields are required (Amount).");
        } else if (user.getBalance().subtract(savingRequest.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundException("You cannot save more than your current balance!.");
        } else if (savingRequest.getSavingType() != SavingType.FLEXIBLE) {
            throw new IncorrectSavingTypeException("Incorrect saving type found.");
        }

        LocalDateTime startDate = LocalDateTime.now();

        Saving saving = new Saving();
        saving.setUser(user);
        saving.setAmount(savingRequest.getAmount());
        saving.setDescription(savingRequest.getDescription());
        saving.setIsActive(true);
        saving.setSavingType(SavingType.FLEXIBLE);
        saving.setInterestEarned(BigDecimal.ZERO);
        saving.setInterestRate(BigDecimal.valueOf(FLEXIBLE_INTEREST_RATE));
        saving.setStartDate(LocalDate.from(startDate));

        savingRepository.save(saving);

        user.setBalance(user.getBalance().subtract(savingRequest.getAmount()));

        userRepository.save(user);
        return SavingResponse.builder()
                .savingType(SavingType.FLEXIBLE)
                .amount(savingRequest.getAmount())
                .startDate(startDate)
                .description(savingRequest.getDescription())
                .message("Saving successfully created!")
                .build();
    }

    @Transactional
    public SavingResponse topUpFlexibleSavingPlan(TopUpSavings savingRequest, UserPrincipal currentUser) {
        User saver = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (saver.getBalance().subtract(savingRequest.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundException("You cannot save more than your current balance!.");
        } else if (savingRequest.getSavingType() == SavingType.FIXED) {
            throw new IncorrectSavingTypeException("Incorrect saving type found.");
        }
        Saving userSaving = savingRepository.findByIdAndUser(savingRequest.getSavingId(), saver);

        BigDecimal newAmount = userSaving.getAmount().add(savingRequest.getAmount());
        userSaving.setAmount(newAmount);
        saver.setBalance(saver.getBalance().subtract(savingRequest.getAmount()));

        userRepository.save(saver);
        SavingHistory savingHistory = SavingHistory.builder()
                    .date(LocalDateTime.now())
                    .type(SavingHistoryTransactionType.DEPOSIT)
                    .amount(savingRequest.getAmount())
                    .savings(userSaving)
                    .build();
        savingHistoryRepository.save(savingHistory);

        return SavingResponse.builder()
                    .amount(savingRequest.getAmount())
                    .description(savingRequest.getDescription())
                    .startDate(LocalDateTime.now())
                    .message("Top up completed!")
                    .build();

    }

    @Transactional
    public SavingResponse withdrawFromFlexibleSaving(WithdrawFromSaving withdrawFromSaving, UserPrincipal currentUser) {
        User saver = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Saving userSaving = savingRepository.findByIdAndUser(withdrawFromSaving.getSavingId(), saver);
        if (userSaving == null)
            throw new SavingNotFoundException("Saving does not exist for user");
        if (userSaving.getAmount().subtract(withdrawFromSaving.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundException("You cannot withdraw more than the amount in your savings");
        } else if (withdrawFromSaving.getSavingType() != SavingType.FLEXIBLE) {
            throw new IncorrectSavingTypeException("Incorrect saving type found.");
        }
        BigDecimal newSavingBalance = userSaving.getAmount().subtract(withdrawFromSaving.getAmount());
        userSaving.setAmount(newSavingBalance);
        BigDecimal newBalance = saver.getBalance().add(withdrawFromSaving.getAmount());
        saver.setBalance(newBalance);
        userRepository.save(saver);
        savingRepository.save(userSaving);
        SavingHistory savingHistory = SavingHistory.builder()
                .date(LocalDateTime.now())
                .type(SavingHistoryTransactionType.WITHDRAWAL)
                .amount(withdrawFromSaving.getAmount())
                .savings(userSaving)
                .build();
        savingHistoryRepository.save(savingHistory);

        return SavingResponse.builder()
                .message("Withdrawal successful!")
                .amount(withdrawFromSaving.getAmount())
                .build();

    }

    public boolean deleteFlexibleSaving(Long id, UserPrincipal currentUser) {
        User foundUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User does not exist/logged in"));
        try {
            Saving userSaving = savingRepository.findByIdAndUser(id, foundUser);
            if (userSaving == null)
                throw  new SavingNotFoundException("Saving does not exist for user");
            BigDecimal allSavedBalance = userSaving.getAmount()
                    .add(userSaving.getInterestEarned().add(userSaving.getAmount()));
            BigDecimal totalEarned = foundUser.getBalance().add(allSavedBalance);

            foundUser.setBalance(foundUser.getBalance().add(totalEarned));
            userRepository.save(foundUser);
            savingRepository.deleteById(id);
            return true;

        } catch (SavingNotFoundException e) {
            return false;
        }
    }


    public SavingResponse createFixedSavingPlan(SavingRequest savingRequest, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + currentUser.getId()));
        LocalDateTime startDate = LocalDateTime.now();

        if (savingRequest.getAmount() == null) {
            throw new IllegalArgumentException("Invalid input: All fields are required (Amount).");
        } else if (user.getBalance().subtract(savingRequest.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundException("You cannot save more than your current balance!.");
        } else if (savingRequest.getSavingType() != SavingType.FIXED) {
            throw new IncorrectSavingTypeException("Incorrect saving type found.");
        } else if (savingRequest.getMaturityDate() == null ) {
            throw new NullPointerException("Maturity for fixed deposit must exist.");
        }  else if (savingRequest.getMaturityDate().isBefore(ChronoLocalDate.from(startDate))) {
            throw new IllegalArgumentException("Maturity for fixed deposit must have a future date");
        }

        Saving saving = new Saving();
        saving.setUser(user);
        saving.setAmount(savingRequest.getAmount());
        saving.setDescription(savingRequest.getDescription());
        saving.setIsActive(true);
        saving.setSavingType(SavingType.FIXED);
        saving.setInterestEarned(BigDecimal.ZERO);
        saving.setInterestRate(BigDecimal.valueOf(FIXED_INTEREST_RATE));
        saving.setStartDate(LocalDate.from(startDate));
        saving.setMaturityDate(savingRequest.getMaturityDate());

        savingRepository.save(saving);

        user.setBalance(user.getBalance().subtract(savingRequest.getAmount()));

        userRepository.save(user);
        return SavingResponse.builder()
                .savingType(SavingType.FIXED)
                .amount(savingRequest.getAmount())
                .startDate(startDate)
                .endDate(savingRequest.getMaturityDate())
                .description(savingRequest.getDescription())
                .message("Saving successfully created!")
                .build();
    }

    public List<Saving> getAllFixedSavings() {
       return savingRepository.findAllBySavingType(SavingType.FIXED);
    }

    public List<Saving> getAllFlexibleSavings() {
        return savingRepository.findAllBySavingType(SavingType.FLEXIBLE);
    }
}


