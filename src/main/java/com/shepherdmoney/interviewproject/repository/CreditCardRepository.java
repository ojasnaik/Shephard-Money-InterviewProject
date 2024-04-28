package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for CreditCard entities.
 * This interface extends JpaRepository and provides methods to interact with the database.
 * It is annotated with @Repository to indicate that it's a Bean and its role is to interact with the database.
 */
@Repository("CreditCardRepo")
public interface CreditCardRepository extends JpaRepository<CreditCard, Integer> {

    /**
     * Method to find a CreditCard entity by its number.
     * @param number The number of the credit card to search for.
     * @return An Optional that contains the CreditCard entity if it exists, or empty if it does not.
     */
    Optional<CreditCard> findByNumber(String number);
}