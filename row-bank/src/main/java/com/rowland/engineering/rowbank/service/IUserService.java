package com.rowland.engineering.rowbank.service;

import com.rowland.engineering.rowbank.dto.MakeDeposit;
import com.rowland.engineering.rowbank.dto.UserResponse;
import com.rowland.engineering.rowbank.dto.UserSummary;
import com.rowland.engineering.rowbank.security.UserPrincipal;

import java.util.Optional;

public interface IUserService {
    void makeDeposit(MakeDeposit deposit, UserPrincipal currentUser);
    Optional<UserResponse> findUserDetails(Long userId);
    UserSummary findUserByAccountNumberOrEmail(String accountNumberOrEmail);
    boolean deleteUser(Long userId);
}
