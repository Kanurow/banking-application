package com.rowland.engineering.rowbank.service;

import com.rowland.engineering.rowbank.dto.MakeDeposit;
import com.rowland.engineering.rowbank.dto.UserResponse;
import com.rowland.engineering.rowbank.dto.UserSummary;
import com.rowland.engineering.rowbank.exception.UserNotFoundException;
import com.rowland.engineering.rowbank.model.Transaction;
import com.rowland.engineering.rowbank.model.TransactionType;
import com.rowland.engineering.rowbank.model.User;
import com.rowland.engineering.rowbank.repository.TransactionRepository;
import com.rowland.engineering.rowbank.repository.UserRepository;
import com.rowland.engineering.rowbank.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    @Transactional
    public void makeDeposit(MakeDeposit deposit, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User with id: " + currentUser.getId() + " not found"));
        user.setBalance(user.getBalance().add(deposit.getDepositAmount()));

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.CREDIT)
                .bankName(user.getBankName())
                .amount(deposit.getDepositAmount())
                .description(deposit.getDescription())
                .timestamp(LocalDateTime.now())
                .user(user)
                .build();

        transactionRepository.save(transaction);
        userRepository.save(user);
    }

    public Optional<UserResponse> findUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

        UserResponse userDetail = new UserResponse();
        userDetail.setId(user.getId());
        userDetail.setUsername(user.getUsername());
        userDetail.setAccountNumber(user.getAccountNumber());
        userDetail.setEmail(user.getEmail());
        userDetail.setBalance(user.getBalance());
        userDetail.setFirstName(user.getFirstName());
        userDetail.setLastName(user.getLastName());
        userDetail.setDateOfBirth(user.getDateOfBirth());
        userDetail.setRoles(user.getRoles());

        return Optional.of(userDetail);
    }

    public UserSummary findUserByAccountNumberOrEmail(String accountNumberOrEmail) {
        User user = userRepository.findByAccountNumberOrEmail(accountNumberOrEmail, accountNumberOrEmail)
                .orElseThrow(() -> new UserNotFoundException("Please confirm account information!"));

        return UserSummary.builder()
                .id(user.getId())
                .accountNumber(user.getAccountNumber())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public boolean deleteUser(Long userId) {
        try{
            userRepository.deleteById(userId);
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
    }
}
