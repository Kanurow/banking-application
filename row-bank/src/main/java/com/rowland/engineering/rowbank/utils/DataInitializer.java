package com.rowland.engineering.rowbank.utils;

import com.rowland.engineering.rowbank.exception.AppException;
import com.rowland.engineering.rowbank.model.BankName;
import com.rowland.engineering.rowbank.model.Role;
import com.rowland.engineering.rowbank.model.RoleName;
import com.rowland.engineering.rowbank.model.User;
import com.rowland.engineering.rowbank.repository.RoleRepository;
import com.rowland.engineering.rowbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.rowland.engineering.rowbank.model.RoleName.ROLE_ADMIN;
import static com.rowland.engineering.rowbank.model.RoleName.ROLE_USER;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Role> roles = Arrays.asList(
                new Role(ROLE_USER),
                new Role(ROLE_ADMIN)
        );
        roleRepository.saveAll(roles);
        persistUsers();
    }
    private void persistUsers() {

        Role roleUser = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));
        Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new AppException("Admin Role not set."));

        User user1 = User.builder()
                .id(1L)
                .firstName("Rowland")
                .lastName("Kanu")
                .dateOfBirth(LocalDate.of(2007, 11,20))
                .username("flames")
                .email("kanurowland92@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2243234870")
                .balance(BigDecimal.valueOf(800))
                .bankName(BankName.ACCESS_BANK)
                .build();

        User user2 = User.builder()
                .id(2L)
                .firstName("Samuel")
                .lastName("Kanu")
                .dateOfBirth(LocalDate.of(1999, 4,15))
                .username("chinonyerem")
                .email("kanurow12@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2255234875")
                .balance(BigDecimal.valueOf(500.23))
                .bankName(BankName.ECO_BANK)
                .build();

        User user3 = User.builder()
                .id(3L)
                .firstName("Gabriel")
                .lastName("Dan")
                .dateOfBirth(LocalDate.of(2000, 1,30))
                .username("gabriel")
                .email("kanugabriel@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2277234875")
                .balance(BigDecimal.valueOf(200))
                .bankName(BankName.FIDELITY_BANK)
                .build();

        User user4 = User.builder()
                .id(4L)
                .firstName("Sussan")
                .lastName("Ethan")
                .dateOfBirth(LocalDate.of(2011, 5,10))
                .username("sussy1")
                .email("sussyrow12@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2233874871")
                .balance(BigDecimal.valueOf(4000))
                .bankName(BankName.ROW_BANK)
                .build();

        User user5 = User.builder()
                .id(5L)
                .firstName("Juliet")
                .lastName("Kanu")
                .dateOfBirth(LocalDate.of(1998, 2,12))
                .username("sammykanu")
                .email("julietkanu@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2255234812")
                .balance(BigDecimal.valueOf(65600))
                .bankName(BankName.ECO_BANK)
                .build();

        User user6 = User.builder()
                .id(6L)
                .firstName("Posh")
                .lastName("Kanu")
                .dateOfBirth(LocalDate.of(2004, 12,2))
                .username("posh")
                .email("kanurow1@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2260914875")
                .balance(BigDecimal.valueOf(65300))
                .bankName(BankName.UNION_BANK)
                .build();

        User user7 = User.builder()
                .id(7L)
                .firstName("Kross")
                .lastName("Lampard")
                .dateOfBirth(LocalDate.of(2008, 1,23))
                .username("kross")
                .email("kross@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2255238015")
                .balance(BigDecimal.valueOf(95000.23))
                .bankName(BankName.ROW_BANK)
                .build();

        User user8 = User.builder()
                .id(8L)
                .firstName("Grace")
                .lastName("Isreal")
                .dateOfBirth(LocalDate.of(1998, 8,8))
                .username("Isreal")
                .email("Isreal@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2255192075")
                .balance(BigDecimal.valueOf(37150.32))
                .bankName(BankName.UNION_BANK)
                .build();

        User user9 = User.builder()
                .id(9L)
                .firstName("Lewis")
                .lastName("Ugwa")
                .dateOfBirth(LocalDate.of(2000, 1,17))
                .username("Qing")
                .email("Lewisugwu@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2255238075")
                .balance(BigDecimal.valueOf(549000.23))
                .bankName(BankName.ROW_BANK)
                .build();

        User user10 = User.builder()
                .id(10L)
                .firstName("Chisom")
                .lastName("Ugwu")
                .dateOfBirth(LocalDate.of(2020, 2,13))
                .username("Chisom12")
                .email("Chisom32@gmail.com")
                .password(passwordEncoder.encode("flames"))
                .accountNumber("2287658175")
                .balance(BigDecimal.valueOf(95000.23))
                .bankName(BankName.UNITED_BANK_FOR_AFRICA)
                .build();

        user1.setRoles(new HashSet<>(List.of(roleUser, roleAdmin)));
        user2.setRoles(new HashSet<>(List.of(roleAdmin)));
        user3.setRoles(new HashSet<>(List.of(roleUser, roleAdmin)));
        user4.setRoles(new HashSet<>(List.of(roleUser)));
        user5.setRoles(new HashSet<>(List.of(roleUser)));
        user6.setRoles(new HashSet<>(List.of(roleUser, roleAdmin)));
        user7.setRoles(new HashSet<>(List.of(roleUser)));
        user8.setRoles(new HashSet<>(List.of(roleAdmin)));
        user9.setRoles(new HashSet<>(List.of(roleUser, roleAdmin)));
        user10.setRoles(new HashSet<>(List.of(roleUser, roleAdmin)));

        userRepository.saveAll(List.of(
                user1, user2, user3, user4, user5,
                user6, user7, user8, user9, user10));
    }

}

