package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for BalanceHistory entities.
 * This interface extends JpaRepository and provides methods to interact with the database.
 */
@Repository("BalanceHistoryRepo")
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Integer> {

    /**
     * Method to find BalanceHistory entities by date.
     * @param date The date to search for.
     * @return A list of BalanceHistory entities that match the given date.
     */
    List<BalanceHistory> findByDate(LocalDate date);

    /**
     * Method to find BalanceHistory entities by date and credit card ID.
     * @param date The date to search for.
     * @param creditCardId The ID of the credit card to search for.
     * @return A list of BalanceHistory entities that match the given date and credit card ID.
     */
    List<BalanceHistory> findByDateAndCreditCard_Id(LocalDate date, Integer creditCardId);
}