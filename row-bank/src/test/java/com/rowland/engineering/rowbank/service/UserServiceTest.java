package com.rowland.engineering.rowbank.service;

import com.rowland.engineering.rowbank.dto.MakeDeposit;
import com.rowland.engineering.rowbank.dto.UserResponse;
import com.rowland.engineering.rowbank.dto.UserSummary;
import com.rowland.engineering.rowbank.exception.UserNotFoundException;
import com.rowland.engineering.rowbank.model.*;
import com.rowland.engineering.rowbank.repository.TransactionRepository;
import com.rowland.engineering.rowbank.repository.UserRepository;
import com.rowland.engineering.rowbank.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    public void makeDeposit_success() {
        UserPrincipal mockUserPrincipal = new UserPrincipal(1L);
        BigDecimal depositAmount = BigDecimal.valueOf(500);
        User user = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(1000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .dateOfBirth(LocalDate.of(2000,10,10))
                .bankName(BankName.ROW_BANK)
                .build();

        when(userRepository.findById(mockUserPrincipal.getId())).thenReturn(Optional.of(user));

        MakeDeposit deposit = new MakeDeposit();
        deposit.setDepositAmount(depositAmount);
        deposit.setDescription("My first deposit");

        userService.makeDeposit(deposit, mockUserPrincipal);
        verify(userRepository).save(user);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void makeDeposit_userNotFound() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        MakeDeposit deposit = new MakeDeposit();
        deposit.setDepositAmount(BigDecimal.valueOf(50));
        deposit.setDescription("Test Deposit");

        try {
            userService.makeDeposit(deposit, new UserPrincipal(userId));
            fail("Expected UserNotFoundException to be thrown!");
        } catch (UserNotFoundException e) {
            String expectedMessage = "User with id: "+ userId + " not found";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void findUserDetails_success() {
        // Mock user data
        long userId = 2L;
        User user = User.builder()
                .id(userId)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(1000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .dateOfBirth(LocalDate.of(2000,10,10))
                .bankName(BankName.ROW_BANK)
                .password("123456")
                .roles(Set.of(new Role(RoleName.ROLE_USER)))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<UserResponse> userDetails = userService.findUserDetails(userId);

        assertTrue(userDetails.isPresent());
        UserResponse response = userDetails.get();
        assertEquals(userId, response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getAccountNumber(), response.getAccountNumber());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getBalance(), response.getBalance());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(user.getDateOfBirth(), response.getDateOfBirth());
        assertEquals(user.getRoles(), response.getRoles());
    }

    @Test
    public void findUserDetails_UserNotFoundException()  {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> userService.findUserDetails(userId));
        assertEquals(userNotFoundException.getMessage(), "User with id: " + userId + " not found");
    }


    @Test
    public void findUserByAccountNumberOrEmail_success_accountNumber() {
        long userId = 2L;
        User user = User.builder()
                .id(userId)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(1000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .dateOfBirth(LocalDate.of(2000,10,10))
                .bankName(BankName.ROW_BANK)
                .password("123456")
                .roles(Set.of(new Role(RoleName.ROLE_USER)))
                .build();

        when(userRepository.findByAccountNumberOrEmail(user.getAccountNumber(), user.getAccountNumber())).thenReturn(Optional.of(user));

        UserSummary summary = userService.findUserByAccountNumberOrEmail(user.getAccountNumber());

        assertEquals(user.getId(), summary.getId());
        assertEquals(user.getAccountNumber(), summary.getAccountNumber());
        assertEquals(user.getEmail(), summary.getEmail());
        assertEquals(user.getUsername(), summary.getUsername());
        assertEquals(user.getFirstName(), summary.getFirstName());
        assertEquals(user.getLastName(), summary.getLastName());
    }

    @Test
    public void findUserByAccountNumberOrEmail_success_email() {
        long userId = 2L;
        User user = User.builder()
                .id(userId)
                .firstName("Rowland")
                .lastName("Kanu")
                .balance(BigDecimal.valueOf(1000))
                .username("rowland")
                .accountNumber("0011223344")
                .email("kanurowland92@gmail.com")
                .dateOfBirth(LocalDate.of(2000,10,10))
                .bankName(BankName.ROW_BANK)
                .password("123456")
                .roles(Set.of(new Role(RoleName.ROLE_USER)))
                .build();
        when(userRepository.findByAccountNumberOrEmail(user.getEmail(), user.getEmail())).thenReturn(Optional.of(user));

        UserSummary summary = userService.findUserByAccountNumberOrEmail(user.getEmail());

        assertEquals(user.getId(), summary.getId());
        assertEquals(user.getAccountNumber(), summary.getAccountNumber());
        assertEquals(user.getEmail(), summary.getEmail());
        assertEquals(user.getUsername(), summary.getUsername());
        assertEquals(user.getFirstName(), summary.getFirstName());
        assertEquals(user.getLastName(), summary.getLastName());
    }

    @Test
    public void testFindUserByAccountNumberOrEmail_userNotFound() throws Exception {
        String accountNumberOrEmail = "invalidEmailOrAccountNumber";

        when(userRepository.findByAccountNumberOrEmail(accountNumberOrEmail, accountNumberOrEmail)).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException =
                assertThrows(UserNotFoundException.class, () -> userService.findUserByAccountNumberOrEmail(accountNumberOrEmail));
        assertEquals(userNotFoundException.getMessage(), "Please confirm account information!");
    }

    @Test
    public void deleteUser_Success() {
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        boolean result = userService.deleteUser(userId);
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    public void deleteUser_Exception() {
        Long userId = 456L;
        doThrow(UserNotFoundException.class).when(userRepository).deleteById(userId);
        boolean result = userService.deleteUser(userId);
        assertFalse(result);
        verify(userRepository, times(1)).deleteById(userId);
    }

}