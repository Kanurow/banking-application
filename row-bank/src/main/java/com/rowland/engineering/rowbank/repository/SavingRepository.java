package com.rowland.engineering.rowbank.repository;

import com.rowland.engineering.rowbank.model.Saving;
import com.rowland.engineering.rowbank.model.SavingType;
import com.rowland.engineering.rowbank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SavingRepository extends JpaRepository<Saving, Long> {
    List<Saving> findAllByUser(User user);
    Saving findByIdAndUser(Long id, User user);

    List<Saving> findAllByIsActive(boolean isActive);
    List<Saving> findAllBySavingType(SavingType savingType);


//    List<Saving> findAllByMatured(boolean maturityStatus);

//    List<Saving> findByUserAndMaturedIsFalseAndEndDateBeforeOrEqual(User user, LocalDateTime now);
}
