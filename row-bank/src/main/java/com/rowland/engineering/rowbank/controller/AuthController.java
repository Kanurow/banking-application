package com.rowland.engineering.rowbank.controller;

import com.rowland.engineering.rowbank.dto.ApiResponse;
import com.rowland.engineering.rowbank.dto.JwtAuthenticationResponse;
import com.rowland.engineering.rowbank.dto.LoginRequest;
import com.rowland.engineering.rowbank.dto.RegisterRequest;
import com.rowland.engineering.rowbank.exception.AppException;
import com.rowland.engineering.rowbank.model.Role;
import com.rowland.engineering.rowbank.model.RoleName;
import com.rowland.engineering.rowbank.model.User;
import com.rowland.engineering.rowbank.repository.RoleRepository;
import com.rowland.engineering.rowbank.repository.UserRepository;
import com.rowland.engineering.rowbank.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Random;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication - Registration /Sign In")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;



    @Operation(
            summary = "Enables user log in - Users can user either username or email address"
    )
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }


    @Operation(
            summary = "Enables user registration - To sign up with admin role, add `row` to email field."
    )
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if(userRepository.existsByUsername(registerRequest.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        String accountNumber = generateAccountNumber();
        User user = new User(registerRequest.getBankName(), registerRequest.getFirstName(), registerRequest.getLastName(),
                registerRequest.getDateOfBirth(), registerRequest.getUsername(),
                registerRequest.getEmail(), registerRequest.getPassword(),
                registerRequest.getOpeningBalance()
        );
            
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setAccountNumber(accountNumber);

        Role userRole;

        if (registerRequest.getEmail().contains("row")) {
            userRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new AppException("Admin Role not set."));
        } else {
            userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new AppException("User Role not set."));
        }

        user.setRoles(Collections.singleton(userRole));

        User savedUser = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/v1/users/{username}")
                .buildAndExpand(savedUser.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }



    private String generateAccountNumber() {
        Random random = new Random();
        int remainingDigits = random.nextInt(100000000);
        return String.format("22%08d", remainingDigits);
    }


}
