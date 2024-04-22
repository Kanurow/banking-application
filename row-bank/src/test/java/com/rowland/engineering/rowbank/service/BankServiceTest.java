package com.rowland.engineering.rowbank.service;

import com.rowland.engineering.rowbank.dto.*;
import com.rowland.engineering.rowbank.exception.*;
import com.rowland.engineering.rowbank.model.*;
import com.rowland.engineering.rowbank.repository.SavingHistoryRepository;
import com.rowland.engineering.rowbank.repository.SavingRepository;
import com.rowland.engineering.rowbank.repository.TransactionRepository;
import com.rowland.engineering.rowbank.repository.UserRepository;
import com.rowland.engineering.rowbank.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
class BankServiceTest {
    @InjectMocks
    private BankService bankService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SavingRepository savingRepository;

    @Mock
    private SavingHistoryRepository savingHistoryRepository;

    @Test
    public void getBeneficiaryDetails_Success() {

        User beneficiaryAccount = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(900.4))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .dateOfBirth(LocalDate.of(2000,10,10))
                .bankName(BankName.ROW_BANK)
                .build();
        String accountNumberOrEmail = beneficiaryAccount.getEmail();

        Optional<User> userOptional = Optional.of(beneficiaryAccount);

        when(userRepository.findByAccountNumberOrEmail(accountNumberOrEmail, accountNumberOrEmail))
                .thenReturn(userOptional);

        BeneficiaryRequest beneficiaryRequest = new BeneficiaryRequest(accountNumberOrEmail, BankName.ROW_BANK);
        BeneficiaryResponse response = bankService.getBeneficiaryDetails(beneficiaryRequest);

        assertEquals(response.getId(), beneficiaryAccount.getId());
        assertEquals(response.getEmail(), beneficiaryAccount.getEmail());
        assertEquals(response.getFirstName(), beneficiaryAccount.getFirstName());
        assertEquals(response.getLastName(), beneficiaryAccount.getLastName());
        assertEquals(response.getAccountNumber(), beneficiaryAccount.getAccountNumber());
        assertEquals(response.getUsername(), beneficiaryAccount.getUsername());
        assertEquals(response.getBankName(), beneficiaryAccount.getBankName());
    }

    @Test
    public void getBeneficiaryDetails_UserNotFound() throws UserNotFoundException {
        String accountNumberOrEmail = "noUserWithEmailfound@email.com";
        when(userRepository.findByAccountNumberOrEmail(accountNumberOrEmail, accountNumberOrEmail))
                .thenReturn(Optional.empty());

        BeneficiaryRequest beneficiaryRequest = new BeneficiaryRequest(accountNumberOrEmail, BankName.ROW_BANK);
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> bankService.getBeneficiaryDetails(beneficiaryRequest));
        assertEquals(userNotFoundException.getMessage(), accountNumberOrEmail +" not found!!! Check details");
    }
    @Test
    public void getBeneficiaryDetails_EmptyOptional() {
        String accountNumberOrEmail = "noUserWithEmailfound@email.com";
        when(userRepository.findByAccountNumberOrEmail(accountNumberOrEmail, accountNumberOrEmail))
                .thenReturn(Optional.empty());

        BeneficiaryRequest beneficiaryRequest = new BeneficiaryRequest(accountNumberOrEmail, BankName.ROW_BANK);

        try {
            bankService.getBeneficiaryDetails(beneficiaryRequest);
            fail("Expected UserNotFoundException to be thrown!");
        } catch (UserNotFoundException e) {
            String expectedMessage = beneficiaryRequest.getAccountNumberOrEmail() + " not found!!! Check details";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getBeneficiaryDetails_IncorrectBankName() {
        User beneficiaryAccount = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(900.4))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .dateOfBirth(LocalDate.of(2000,10,10))
                .bankName(BankName.ROW_BANK)
                .build();

        when(userRepository.findByAccountNumberOrEmail(beneficiaryAccount.getAccountNumber(), beneficiaryAccount.getAccountNumber()))
                .thenReturn(Optional.of(beneficiaryAccount));

        BeneficiaryRequest beneficiaryRequest = new BeneficiaryRequest(beneficiaryAccount.getAccountNumber(), BankName.ACCESS_BANK);

        try {
            bankService.getBeneficiaryDetails(beneficiaryRequest);
            fail("Expected IncorrectBankNameException to be thrown!");
        } catch (IncorrectBankNameException e) {
            String expectedMessage = beneficiaryRequest.getAccountNumberOrEmail() + " is not a customer at " + beneficiaryRequest.getBankName();
            assertEquals(expectedMessage, e.getMessage());
        }
    }



    @Test
    public void makeTransfer_Success() {
        String transferDescription = "For a new car";
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        User sender = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(3000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .dateOfBirth(LocalDate.of(2000,10,10))
                .bankName(BankName.ROW_BANK)
                .build();

        User receiver = User.builder()
                .id(5L)
                .firstName("Samuel")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(7900))
                .username("samuel09")
                .accountNumber("0055229988")
                .email("kanusamuel92@gmail.com")
                .dateOfBirth(LocalDate.of(2003,2,4))
                .bankName(BankName.FIDELITY_BANK)
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findByAccountNumberOrEmail(receiver.getAccountNumber(), receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));

        // Create MakeTransfer object
        MakeTransfer makeTransfer = new MakeTransfer(transferAmount,
                receiver.getFirstName(), receiver.getLastName(),
                transferDescription, receiver.getAccountNumber(),
                receiver.getBankName(), sender.getBankName());

        // Call the service method
        TransferResponse response = bankService.makeTransfer(makeTransfer, mockUserPrincipal);

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(userRepository).save(sender);
        verify(userRepository).save(receiver);

        // Assertions
        assertEquals(response.getAmount(), transferAmount);
        assertEquals(response.getRemainingSenderBalance(), sender.getBalance());
        assertEquals(response.getReceiverFullName(), receiver.getFirstName() + " " + receiver.getLastName());

    }


    @Test
    public void testMakeTransfer_UserNotFound_Sender() throws UserNotFoundException {
        String transferDescription = "For a new car";
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        User sender = User.builder()
                .id(1L)
                .firstName("Samuel")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(7900))
                .username("samuel09")
                .accountNumber("0055229988")
                .email("kanusamuel92@gmail.com")
                .dateOfBirth(LocalDate.of(2003,2,4))
                .bankName(BankName.UNITED_BANK_FOR_AFRICA)
                .build();
        User receiver = User.builder()
                .id(3L)
                .firstName("James")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(700))
                .username("samuel")
                .accountNumber("0055229911")
                .email("kanusamuel9@gmail.com")
                .dateOfBirth(LocalDate.of(2013,12,14))
                .bankName(BankName.FIDELITY_BANK)
                .build();
        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.empty());
        when(userRepository.findByAccountNumberOrEmail(receiver.getAccountNumber(), receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));

        MakeTransfer makeTransfer = new MakeTransfer(transferAmount,
                receiver.getFirstName(), receiver.getLastName(),
                transferDescription, receiver.getAccountNumber(),
                receiver.getBankName(), sender.getBankName());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> bankService.makeTransfer(makeTransfer, mockUserPrincipal));
        assertEquals(userNotFoundException.getMessage(), "Sender not found! Check details");
    }


    @Test
    public void testMakeTransfer_UserNotFound_Receiver() throws UserNotFoundException {
        String transferDescription = "For a new car";
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        BankName senderBank = BankName.ACCESS_BANK;
        User receiver = User.builder()
                .id(5L)
                .firstName("Samuel")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(7900))
                .username("samuel09")
                .accountNumber("0055229988")
                .email("kanusamuel92@gmail.com")
                .dateOfBirth(LocalDate.of(2003,2,4))
                .bankName(BankName.FIDELITY_BANK)
                .build();
        when(userRepository.findByAccountNumberOrEmail(receiver.getAccountNumber(), receiver.getAccountNumber()))
                .thenReturn(Optional.empty());
        MakeTransfer makeTransfer = new MakeTransfer(transferAmount,
                receiver.getFirstName(), receiver.getLastName(),
                transferDescription, receiver.getAccountNumber(),
                receiver.getBankName(), senderBank);

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> bankService.makeTransfer(makeTransfer, mockUserPrincipal));
        assertEquals(userNotFoundException.getMessage(), "Receiver not found! Check details");
    }


    @Test
    public void testMakeTransfer_IncorrectBankNameException() throws IncorrectBankNameException {
        String transferDescription = "For a new car";
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        BankName incorrectReceiverBankName = BankName.UNION_BANK;
        User receiver = User.builder()
                .id(5L)
                .firstName("Samuel")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(7900))
                .username("samuel09")
                .accountNumber("0055229988")
                .email("kanusamuel92@gmail.com")
                .dateOfBirth(LocalDate.of(2003,2,4))
                .bankName(BankName.FIDELITY_BANK)
                .build();
        User sender = User.builder()
                .id(3L)
                .firstName("James")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(700))
                .username("samuel")
                .accountNumber("0055229911")
                .email("kanusamuel9@gmail.com")
                .dateOfBirth(LocalDate.of(2013,12,14))
                .bankName(BankName.FIDELITY_BANK)
                .build();
        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findByAccountNumberOrEmail(receiver.getAccountNumber(), receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));
        MakeTransfer makeTransfer = new MakeTransfer(transferAmount,
                receiver.getFirstName(), receiver.getLastName(),
                transferDescription, receiver.getAccountNumber(),
                incorrectReceiverBankName, sender.getBankName());

        IncorrectBankNameException incorrectBankNameException = assertThrows(IncorrectBankNameException.class, () -> bankService.makeTransfer(makeTransfer, mockUserPrincipal));
        assertEquals(incorrectBankNameException.getMessage(), "Check bank name details!");
    }



    @Test
    public void testMakeTransfer_AccountDetailsMismatch() throws AccountDetailsMismatch {
        String transferDescription = "For a new car";
        BigDecimal transferAmount = BigDecimal.valueOf(100);
        UserPrincipal sameMockUserPrincipal = new UserPrincipal(1L);

        User sameUser = User.builder()
                .id(1L)
                .firstName("Samuel")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(7900))
                .username("samuel09")
                .accountNumber("0055229988")
                .email("kanusamuel92@gmail.com")
                .dateOfBirth(LocalDate.of(2003,2,4))
                .bankName(BankName.GUARANTY_TRUST_BANK)
                .build();


        when(userRepository.findById(sameMockUserPrincipal.getId())).thenReturn(Optional.of(sameUser));
        when(userRepository.findByAccountNumberOrEmail(sameUser.getAccountNumber(), sameUser.getAccountNumber()))
                .thenReturn(Optional.of(sameUser));
        MakeTransfer makeTransfer = new MakeTransfer(transferAmount,
                sameUser.getFirstName(), sameUser.getLastName(),
                transferDescription, sameUser.getAccountNumber(),
                sameUser.getBankName(), sameUser.getBankName());

        AccountDetailsMismatch accountDetailsMismatch = assertThrows(AccountDetailsMismatch.class, () -> bankService.makeTransfer(makeTransfer, sameMockUserPrincipal));
        assertEquals(accountDetailsMismatch.getMessage(), "You cannot transfer into your own account");
    }



    @Test
    public void testMakeTransfer_InsufficientFundException() throws InsufficientFundException {
        String transferDescription = "For a new car";
        BigDecimal transferAmount = BigDecimal.valueOf(5000);
        UserPrincipal sameMockUserPrincipal = new UserPrincipal(1L);

        User receiver = User.builder()
                .id(5L)
                .firstName("Samuel")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(7900))
                .username("samuel09")
                .accountNumber("0055229988")
                .email("kanusamuel92@gmail.com")
                .dateOfBirth(LocalDate.of(2003,2,4))
                .bankName(BankName.FIDELITY_BANK)
                .build();
        User sender = User.builder()
                .id(3L)
                .firstName("James")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(700))
                .username("samuel")
                .accountNumber("0055229911")
                .email("kanusamuel9@gmail.com")
                .dateOfBirth(LocalDate.of(2013,12,14))
                .bankName(BankName.FIDELITY_BANK)
                .build();


        when(userRepository.findById(sameMockUserPrincipal.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findByAccountNumberOrEmail(receiver.getAccountNumber(), receiver.getAccountNumber()))
                .thenReturn(Optional.of(receiver));
        MakeTransfer makeTransfer = new MakeTransfer(transferAmount,
                receiver.getFirstName(), receiver.getLastName(),
                transferDescription, receiver.getAccountNumber(),
                receiver.getBankName(), sender.getBankName());

        InsufficientFundException insufficientFundException = assertThrows(InsufficientFundException.class, () -> bankService.makeTransfer(makeTransfer, sameMockUserPrincipal));
        assertEquals(insufficientFundException.getMessage(), "Insufficient fund to cover transfer");
    }

    @Test
    public void createFlexibleSavingPlan_Success() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        User saver = User.builder()
                .id(2L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();


        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(200))
                .description("Test saving for new car")
                .maturityDate(LocalDate.of(2000, 2, 2))
                .savingType(SavingType.FLEXIBLE)
                .build();


        Saving saving = new Saving();
        saving.setUser(saver);
        saving.setAmount(savingRequest.getAmount());
        saving.setDescription(savingRequest.getDescription());
        saving.setIsActive(true);
        saving.setSavingType(SavingType.FLEXIBLE);
        saving.setInterestEarned(BigDecimal.ZERO);
        saving.setInterestRate(BigDecimal.valueOf(0.07));
        saving.setStartDate(LocalDate.of(2000,2,2));


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));
        when(savingRepository.save(saving)).thenReturn(saving);

        SavingResponse response = bankService.createFlexibleSavingPlan(savingRequest, mockUserPrincipal);

        verify(userRepository, times(1)).save(any(User.class));
        verify(savingRepository, times(1)).save(any(Saving.class));


        assertEquals(response.getAmount(), savingRequest.getAmount());
        assertEquals(response.getDescription(), savingRequest.getDescription());
        assertEquals(response.getMessage(), "Saving successfully created!");
        assertEquals(response.getSavingType(), savingRequest.getSavingType());

        Optional<User> savedUser = userRepository.findById(mockUserPrincipal.getId());
        assertTrue(savedUser.isPresent());
        assertEquals(savedUser.get().getBalance(), saver.getBalance());
        assertEquals(saving.getUser(), savedUser.get());
    }



    @Test
    public void createFlexibleSavingPlan_UserNotFoundException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(200))
                .description("Test saving for new car")
                .maturityDate(LocalDate.of(2000, 2, 2))
                .savingType(SavingType.FLEXIBLE)
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> bankService.createFlexibleSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(userNotFoundException.getMessage(), "User not found with ID: " + mockUserPrincipal.getId());
    }


    @Test
    public void createFlexibleSavingPlan_IllegalArgumentException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        SavingRequest savingRequest = SavingRequest.builder()
                .amount(null)
                .description("Test saving for new car")
                .maturityDate(LocalDate.of(2000, 2, 2))
                .savingType(SavingType.FLEXIBLE)
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> bankService.createFlexibleSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(illegalArgumentException.getMessage(), "Invalid input: All fields are required (Amount).");
    }


    @Test
    public void createFlexibleSavingPlan_InsufficientFundException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(10000))
                .description("Test saving for new car")
                .maturityDate(LocalDate.of(2000, 2, 2))
                .savingType(SavingType.FLEXIBLE)
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));

        InsufficientFundException insufficientFundException = assertThrows(InsufficientFundException.class, () -> bankService.createFlexibleSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(insufficientFundException.getMessage(), "You cannot save more than your current balance!.");
    }



    @Test
    public void createFlexibleSavingPlan_IncorrectSavingTypeException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(1000))
                .description("Test saving for new car")
                .maturityDate(LocalDate.of(2000, 2, 2))
                .savingType(SavingType.FIXED)
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));

        IncorrectSavingTypeException incorrectSavingTypeException = assertThrows(IncorrectSavingTypeException.class, () -> bankService.createFlexibleSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(incorrectSavingTypeException.getMessage(), "Incorrect saving type found.");
    }

    @Test
    void topUpFlexibleSavingPlan_Success() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();

        TopUpSavings topUpSavings = TopUpSavings.builder()
                .savingId(2L)
                .amount(BigDecimal.valueOf(200))
                .description("Top up my saving for new car")
                .savingType(SavingType.FLEXIBLE)
                .build();

        Saving saving = Saving.builder()
                .id(2L)
                .isActive(true)
                .savingType(SavingType.FLEXIBLE)
                .user(saver)
                .amount(BigDecimal.valueOf(400))
                .description("New Car")
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));
        when(savingRepository.findByIdAndUser(topUpSavings.getSavingId(), saver)).thenReturn(saving);

        SavingResponse response = bankService.topUpFlexibleSavingPlan(topUpSavings, mockUserPrincipal);

        verify(savingHistoryRepository, times(1)).save(any(SavingHistory.class));
        verify(userRepository, times(1)).save(any(User.class));

        assertEquals(response.getAmount(), topUpSavings.getAmount());
        assertEquals(response.getMessage(), "Top up completed!");
        assertEquals(response.getDescription(), topUpSavings.getDescription());
    }

    @Test
    void topUpFlexibleSavingPlan_UserNotFoundException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);

        TopUpSavings topUpSavings = TopUpSavings.builder()
                .savingId(2L)
                .amount(BigDecimal.valueOf(200))
                .description("Top up my saving for new car")
                .savingType(SavingType.FLEXIBLE)
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> bankService.topUpFlexibleSavingPlan(topUpSavings, mockUserPrincipal));
        assertEquals(userNotFoundException.getMessage(), "User not found");
    }


    @Test
    void topUpFlexibleSavingPlan_InsufficientFundException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();

        TopUpSavings topUpSavings = TopUpSavings.builder()
                .savingId(2L)
                .amount(BigDecimal.valueOf(20000))
                .description("Top up my saving for new car")
                .savingType(SavingType.FLEXIBLE)
                .build();


        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));

        InsufficientFundException insufficientFundException = assertThrows(InsufficientFundException.class, () -> bankService.topUpFlexibleSavingPlan(topUpSavings, mockUserPrincipal));
        assertEquals(insufficientFundException.getMessage(), "You cannot save more than your current balance!.");
    }


    @Test
    void topUpFlexibleSavingPlan_IncorrectSavingTypeException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();

        TopUpSavings topUpSavings = TopUpSavings.builder()
                .savingId(2L)
                .amount(BigDecimal.valueOf(200))
                .description("Top up my saving for new car")
                .savingType(SavingType.FIXED)
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));

        IncorrectSavingTypeException incorrectSavingTypeException = assertThrows(IncorrectSavingTypeException.class, () -> bankService.topUpFlexibleSavingPlan(topUpSavings, mockUserPrincipal));
        assertEquals(incorrectSavingTypeException.getMessage(), "Incorrect saving type found.");
    }


    @Test
    void withdrawFromFlexibleSaving_Success() {
        UserPrincipal mockUserPrinciple = new UserPrincipal(1L);
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        WithdrawFromSaving withdraw = WithdrawFromSaving.builder()
                .savingId(5L)
                .amount(BigDecimal.valueOf(300))
                .description("Emergency")
                .savingType(SavingType.FLEXIBLE)
                .build();
        Saving saving = Saving.builder()
                .id(5L)
                .amount(BigDecimal.valueOf(500))
                .isActive(true)
                .savingType(SavingType.FLEXIBLE)
                .build();
        when(userRepository.findById(mockUserPrinciple.getId())).thenReturn(Optional.of(saver));
        when(savingRepository.findByIdAndUser(withdraw.getSavingId(), saver)).thenReturn(saving);
        SavingResponse savingResponse = bankService.withdrawFromFlexibleSaving(withdraw, mockUserPrinciple);
        assertEquals(savingResponse.getMessage(), "Withdrawal successful!");
        assertEquals(savingResponse.getAmount(), withdraw.getAmount());

        verify(savingHistoryRepository, times(1)).save(any(SavingHistory.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(savingRepository, times(1)).save(any(Saving.class));
    }


    @Test
    void withdrawFromFlexibleSaving_SavingNotFoundException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        WithdrawFromSaving withdraw = WithdrawFromSaving.builder()
                .savingId(5L)
                .amount(BigDecimal.valueOf(300))
                .description("Emergency")
                .savingType(SavingType.FLEXIBLE)
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));
        when(savingRepository.findByIdAndUser(withdraw.getSavingId(), saver)).thenReturn(null);


        SavingNotFoundException savingNotFoundException = assertThrows(SavingNotFoundException.class, () -> bankService.withdrawFromFlexibleSaving(withdraw, mockUserPrincipal));
        assertEquals(savingNotFoundException.getMessage(), "Saving does not exist for user");
    }


    @Test
    void withdrawFromFlexibleSaving_UserNotFoundException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        WithdrawFromSaving withdraw = WithdrawFromSaving.builder()
                .savingId(5L)
                .amount(BigDecimal.valueOf(300))
                .description("Emergency")
                .savingType(SavingType.FLEXIBLE)
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> bankService.withdrawFromFlexibleSaving(withdraw, mockUserPrincipal));
        assertEquals(userNotFoundException.getMessage(), "User not found");
    }


    @Test
    void withdrawFromFlexibleSaving_InsufficientFundException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        Saving saving = Saving.builder()
                .id(5L)
                .amount(BigDecimal.valueOf(500))
                .isActive(true)
                .savingType(SavingType.FLEXIBLE)
                .build();
        WithdrawFromSaving withdraw = WithdrawFromSaving.builder()
                .savingId(5L)
                .amount(BigDecimal.valueOf(3000))
                .description("Emergency")
                .savingType(SavingType.FLEXIBLE)
                .build();

        when(savingRepository.findByIdAndUser(withdraw.getSavingId(), saver)).thenReturn(saving);
        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));
        InsufficientFundException insufficientFundException = assertThrows(InsufficientFundException.class, () -> bankService.withdrawFromFlexibleSaving(withdraw, mockUserPrincipal));
        assertEquals(insufficientFundException.getMessage(), "You cannot withdraw more than the amount in your savings");
    }


    @Test
    void withdrawFromFlexibleSaving_IncorrectSavingTypeException() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        Saving saving = Saving.builder()
                .id(5L)
                .amount(BigDecimal.valueOf(500))
                .isActive(true)
                .savingType(SavingType.FLEXIBLE)
                .build();
        WithdrawFromSaving withdraw = WithdrawFromSaving.builder()
                .savingId(5L)
                .amount(BigDecimal.valueOf(300))
                .description("Emergency")
                .savingType(SavingType.FIXED)
                .build();

        when(savingRepository.findByIdAndUser(withdraw.getSavingId(), saver)).thenReturn(saving);
        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(saver));
        IncorrectSavingTypeException incorrectSavingTypeException = assertThrows(IncorrectSavingTypeException.class, () -> bankService.withdrawFromFlexibleSaving(withdraw, mockUserPrincipal));
        assertEquals(incorrectSavingTypeException.getMessage(), "Incorrect saving type found.");
    }

    @Test
    void deleteFlexibleSaving_Success() {
        UserPrincipal userPrincipal = new UserPrincipal(1L);
        Long flexibleSavingIdToBeDeleted = 5L;
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();
        Saving saving = Saving.builder()
                .id(5L)
                .amount(BigDecimal.valueOf(500))
                .interestEarned(BigDecimal.valueOf(90))
                .isActive(true)
                .savingType(SavingType.FLEXIBLE)
                .build();
        when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.of(saver));
        when(savingRepository.findByIdAndUser(flexibleSavingIdToBeDeleted, saver)).thenReturn(saving);
        boolean isDeleted = bankService.deleteFlexibleSaving(flexibleSavingIdToBeDeleted, userPrincipal);
        assertEquals(true, isDeleted);

        verify(savingRepository, times(1)).deleteById(flexibleSavingIdToBeDeleted);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteFlexibleSaving_UserNotFoundException(){
        UserPrincipal userPrincipal = new UserPrincipal(1L);
        Long flexibleSavingIdToBeDeleted = 5L;

        when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.empty());
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> bankService.deleteFlexibleSaving(flexibleSavingIdToBeDeleted, userPrincipal));
        assertEquals(userNotFoundException.getMessage(), "User does not exist/logged in");

    }


    @Test
    void deleteFlexibleSaving_SavingNotFoundException() {
        UserPrincipal userPrincipal = new UserPrincipal(1L);
        Long flexibleSavingIdToBeDeleted = 5L;
        User saver = User.builder()
                .id(1L)
                .lastName("Kanu")
                .firstName("Rowland")
                .balance(BigDecimal.valueOf(4000))
                .username("flames")
                .bankName(BankName.ROW_BANK)
                .email("Kanurowland92@gmail.com")
                .accountNumber("1122334455")
                .build();

        when(userRepository.findById(userPrincipal.getId())).thenReturn(Optional.of(saver));
        when(savingRepository.findByIdAndUser(flexibleSavingIdToBeDeleted, saver)).thenReturn(null);

        boolean response = bankService.deleteFlexibleSaving(flexibleSavingIdToBeDeleted, userPrincipal);
        assertFalse(response);
        verify(userRepository, never()).save(any(User.class));
        verify(savingRepository, never()).deleteById(any(Long.class));

    }



    @Test
    public void getBeneficiaryDetails_happyPath() {
        // Arrange
        BeneficiaryRequest request = new BeneficiaryRequest("kanurowland92@gmail.com", BankName.ROW_BANK);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(900.4))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();
        when(userRepository.findByAccountNumberOrEmail(request.getAccountNumberOrEmail(), request.getAccountNumberOrEmail())).thenReturn(Optional.of(user));

        // Act
        BeneficiaryResponse response = bankService.getBeneficiaryDetails(request);

        // Assert
        assertAll(
                () -> assertEquals("kanurowland92@gmail.com", response.getEmail()),
                () -> assertEquals("0011223344", response.getAccountNumber()),
                () -> assertEquals("rowland", response.getUsername()),
                () -> assertEquals("Rowland", response.getFirstName()),
                () -> assertEquals("Kanu", response.getLastName()),
                () -> assertEquals(response.getBankName(), BankName.ROW_BANK)
        );
    }

    @Test
    void createFixedSavingPlan_Success() {
        UserPrincipal mockUserPrincipal =  new UserPrincipal(1L);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(900.4))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();
        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(300))
                .savingType(SavingType.FIXED)
                .maturityDate(LocalDate.of(2025,5,20))
                .description("New Car Fixed Saving")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.ofNullable(user));
        SavingResponse response = bankService.createFixedSavingPlan(savingRequest, mockUserPrincipal);
        assertEquals(response.getMessage(), "Saving successfully created!");
        assertEquals(response.getDescription(), savingRequest.getDescription());
        assertEquals(response.getEndDate(), savingRequest.getMaturityDate());
        assertEquals(response.getSavingType(), savingRequest.getSavingType());

        assertEquals(response.getStartDate().getYear(), LocalDateTime.now().getYear());
        assertEquals(response.getStartDate().getMonth(), LocalDateTime.now().getMonth());
        assertEquals(response.getStartDate().getDayOfMonth(), LocalDateTime.now().getDayOfMonth());

        assert user != null;
        verify(userRepository, times(1)).save(user);
        verify(savingRepository, times(1)).save(any(Saving.class));

    }


    @Test
    void createFixedSavingPlan_UserNotFoundException() {
        UserPrincipal mockUserPrincipal =  new UserPrincipal(1L);

        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(300))
                .savingType(SavingType.FIXED)
                .maturityDate(LocalDate.of(2025,5,20))
                .description("New Car Fixed Saving")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> bankService.createFixedSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(userNotFoundException.getMessage(), "User not found with ID: " + mockUserPrincipal.getId());
    }


    @Test
    void createFixedSavingPlan_IllegalArgumentException() {
        UserPrincipal mockUserPrincipal =  new UserPrincipal(1L);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(900.4))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();

        SavingRequest savingRequest = SavingRequest.builder()
                .savingType(SavingType.FIXED)
                .maturityDate(LocalDate.of(2025,5,20))
                .description("New Car Fixed Saving")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(user));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> bankService.createFixedSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(illegalArgumentException.getMessage(), "Invalid input: All fields are required (Amount).");
    }



    @Test
    void createFixedSavingPlan_InsufficientFundException() {
        UserPrincipal mockUserPrincipal =  new UserPrincipal(1L);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(3000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();

        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(4000))
                .savingType(SavingType.FIXED)
                .maturityDate(LocalDate.of(2025,5,20))
                .description("New Car Fixed Saving")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(user));

        InsufficientFundException insufficientFundException = assertThrows(InsufficientFundException.class,
                () -> bankService.createFixedSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(insufficientFundException.getMessage(), "You cannot save more than your current balance!.");
    }


    @Test
    void createFixedSavingPlan_IncorrectSavingTypeException() {
        UserPrincipal mockUserPrincipal =  new UserPrincipal(1L);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(3000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();

        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(500))
                .savingType(SavingType.FLEXIBLE)
                .maturityDate(LocalDate.of(2025,5,20))
                .description("New Car Fixed Saving")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(user));

        IncorrectSavingTypeException incorrectSavingTypeException = assertThrows(IncorrectSavingTypeException.class,
                () -> bankService.createFixedSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(incorrectSavingTypeException.getMessage(), "Incorrect saving type found.");
    }


    @Test
    void createFixedSavingPlan_IllegalArgumentException_maturityDateIsNull() {
        UserPrincipal mockUserPrincipal =  new UserPrincipal(1L);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(3000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();

        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(500))
                .maturityDate(null)
                .savingType(SavingType.FIXED)
                .description("New Car Fixed Saving")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(user));

        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> bankService.createFixedSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(nullPointerException.getMessage(), "Maturity for fixed deposit must exist.");
    }



    @Test
    void createFixedSavingPlan_IllegalArgumentException_maturityDateIsInThePast() {
        UserPrincipal mockUserPrincipal =  new UserPrincipal(1L);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(3000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();

        SavingRequest savingRequest = SavingRequest.builder()
                .amount(BigDecimal.valueOf(500))
                .maturityDate(LocalDate.of(2012,8,5))
                .savingType(SavingType.FIXED)
                .description("New Car Fixed Saving")
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(user));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> bankService.createFixedSavingPlan(savingRequest, mockUserPrincipal));
        assertEquals(illegalArgumentException.getMessage(), "Maturity for fixed deposit must have a future date");
    }



    @Test
    public void getAllFixedSavings_withFixedSavings() {
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(900.4))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();
        List<Saving> fixedSavings = Arrays.asList(
                new Saving(1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(0.07), BigDecimal.valueOf(900),
                        LocalDate.now(), "Fixed savings", true, LocalDate.of(2025,11,12), SavingType.FIXED, user),
                new Saving(2L, BigDecimal.valueOf(2000), BigDecimal.valueOf(0.09), BigDecimal.valueOf(1200),
                        LocalDate.now(), "Another fixed saving", true, LocalDate.of(2025,10,22), SavingType.FIXED, user)
        );

        when(savingRepository.findAllBySavingType(SavingType.FIXED)).thenReturn(fixedSavings);

        List<Saving> retrievedSavings = bankService.getAllFixedSavings();

        // Assertions
        assertEquals(2, retrievedSavings.size());
        assertEquals(fixedSavings, retrievedSavings);
    }

    @Test
    public void getAllFixedSavings_withNoFixedSavings() {
        when(savingRepository.findAllBySavingType(SavingType.FIXED)).thenReturn(Collections.emptyList());
        List<Saving> retrievedSavings = bankService.getAllFixedSavings();

        assertTrue(retrievedSavings.isEmpty());
    }


    @Test
    public void getAllFlexibleSavings_withFlexibleSavings() {
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(1000.4))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .bankName(BankName.ROW_BANK)
                .build();
        List<Saving> flexibleSavings = Arrays.asList(
                new Saving(1L, BigDecimal.valueOf(5000), BigDecimal.valueOf(0.07), BigDecimal.valueOf(900),
                        LocalDate.now(), "Fixed savings for new car", true, LocalDate.of(2025,11,12), SavingType.FLEXIBLE, user),
                new Saving(2L, BigDecimal.valueOf(2000), BigDecimal.valueOf(0.09), BigDecimal.valueOf(1200),
                        LocalDate.now(), "Fixed saving for new house", true, LocalDate.of(2025,10,22), SavingType.FLEXIBLE, user)
        );

        when(savingRepository.findAllBySavingType(SavingType.FLEXIBLE)).thenReturn(flexibleSavings);

        List<Saving> retrievedSavings = bankService.getAllFlexibleSavings();

        assertEquals(2, retrievedSavings.size());
        assertEquals(flexibleSavings, retrievedSavings);
    }

    @Test
    public void getAllFlexibleSavings_withNoFlexibleSavings() {
        when(savingRepository.findAllBySavingType(SavingType.FLEXIBLE)).thenReturn(Collections.emptyList());
        List<Saving> retrievedSavings = bankService.getAllFlexibleSavings();

        assertTrue(retrievedSavings.isEmpty());
    }

}