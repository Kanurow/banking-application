package com.rowland.engineering.rowbank.model;

import com.rowland.engineering.rowbank.model.audit.DateAudit;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Builder
@Entity
@AllArgsConstructor
@Table(name = "users_table", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "username"
        }),
        @UniqueConstraint(columnNames = {
                "account_number"
        }),
        @UniqueConstraint(columnNames = {
                "email"
        })
})
public class User extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(min = 2,max = 25)
    private String firstName;
    @NotBlank
    @Size(min = 2,max = 25)
    private String lastName;

    @NotBlank
    @Size(min = 2,max = 20)
    @NaturalId
    private String username;

    @NaturalId
    @NotBlank(message = "User must have an account number")
    private String accountNumber;

    @PastOrPresent(message = "Date of birth cannot be in future")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfBirth;

    @NaturalId
    @Email(message = "Must be a valid email address")
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    private BankName bankName;

    @DecimalMin(value = "0.0", message = "Balance must be 0.0 or greater")
    private BigDecimal balance;
    @NotBlank
    private String password;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();


    public User(BankName bankName,String firstName, String lastName, LocalDate dateOfBirth,
                String username, String email, String password, BigDecimal balance) {
        this.bankName = bankName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.username = username;
        this.email = email;
        this.password = password;
        this.balance = balance;
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", bankName='" + bankName + '\'' +
                ", balance='" + balance + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                '}';
    }
}
