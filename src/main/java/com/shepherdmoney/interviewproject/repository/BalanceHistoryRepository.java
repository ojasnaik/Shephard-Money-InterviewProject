package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository("BalanceHistoryRepo")
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Integer> {
    List<BalanceHistory> findByDate(LocalDate date);

    List<BalanceHistory> findByDateAndCreditCard_Id(LocalDate date, Integer creditCardId);
}