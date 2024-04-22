package com.rowland.engineering.rowbank.utils;

import com.rowland.engineering.rowbank.model.Saving;
import com.rowland.engineering.rowbank.model.User;
import com.rowland.engineering.rowbank.repository.SavingRepository;
import com.rowland.engineering.rowbank.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InterestCalculator {

    private final SavingRepository savingRepository;
    private final UserRepository userRepository;
    @Transactional
    @Scheduled(fixedRate = 86400000)
    public void calculateDailyInterest() {
        List<Saving> activeSavings = savingRepository.findAllByIsActive(true);

        for (Saving saving : activeSavings) {
            if (saving.getMaturityDate().isAfter(LocalDate.now())) {
                saving.setIsActive(false);
                User user = saving.getUser();
                user.setBalance(user.getBalance()
                        .add(saving.getInterestEarned().add(saving.getAmount())
                        ));
                userRepository.save(user);
                continue;
            }
            BigDecimal dailyInterest = calculateDailyInterest(saving.getAmount(), saving.getInterestRate());
            saving.setInterestEarned(saving.getInterestEarned().add(dailyInterest));
            savingRepository.save(saving);
        }
    }

    private BigDecimal calculateDailyInterest(BigDecimal amount, BigDecimal interestRate) {
        BigDecimal daysInYear = BigDecimal.valueOf(365);
        return amount.multiply(interestRate).multiply(daysInYear).divide(BigDecimal.valueOf(36500), RoundingMode.HALF_UP);
    }

}
