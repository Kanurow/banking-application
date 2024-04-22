package com.rowland.engineering.rowbank.controller;


import com.rowland.engineering.rowbank.dto.*;
import com.rowland.engineering.rowbank.security.CurrentUser;
import com.rowland.engineering.rowbank.security.UserPrincipal;
import com.rowland.engineering.rowbank.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final IUserService userService;
    @Operation(
            summary = "Make deposit into user personal account",
            description = "Allowing users increase there deposit balance while trying out the application"
    )
    @PatchMapping("/make-deposit")
    public ResponseEntity<ApiResponse> depositIntoUserAccount(@Valid @CurrentUser UserPrincipal currentUser,
                                                              @RequestBody MakeDeposit deposit) {
        userService.makeDeposit(deposit, currentUser);
        return ResponseEntity.ok(new ApiResponse(true,
                "Account successfully credited with: #"+ deposit.getDepositAmount()));
    }

    @Operation(
            description = "Get user by Id",
            summary = "Returns user by providing user id"
    )
    @GetMapping("/{id}")
    public Optional<UserResponse> getUserById(@PathVariable(value = "id") Long userId) {
        return userService.findUserDetails(userId);
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER'), hasRole('ADMIN')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        return new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getFirstName(),
                currentUser.getLastName(), currentUser.getAccountNumber(), currentUser.getEmail());
    }

    @Operation(
            summary = "Find user by account number or email"
    )
    @GetMapping("/find-user/{accountNumberOrEmail}")
    public UserSummary getUserByAccountNumberOrEmail(@PathVariable(value = "accountNumberOrEmail") String accountNumberOrEmail) {
        return userService.findUserByAccountNumberOrEmail(accountNumberOrEmail);
    }

    @Operation(
            summary = "Delete user account -> Can only be done by admin level users"
    )
    @DeleteMapping("/user/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable(value = "userId") Long userId) {
        boolean isDeleted = userService.deleteUser(userId);
        if (isDeleted)
            return new ResponseEntity<>("User has been successfully deleted", HttpStatus.NO_CONTENT);
        return new ResponseEntity<>("Error! User Not found", HttpStatus.NOT_FOUND);
    }

}
