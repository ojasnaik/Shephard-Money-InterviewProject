package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for handling credit card related operations.
 */
@RestController
public class CreditCardController {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint to add a credit card to a user.
     *
     * @param payload The payload containing the user ID and credit card details.
     * @return The ID of the created credit card.
     */
    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        Optional<User> user = userRepository.findById(payload.getUserId());
        if (user.isPresent()) {
            CreditCard creditCard = new CreditCard();
            creditCard.setNumber(payload.getCardNumber());
            creditCard.setOwner(user.get());
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCardRepository.save(creditCard);
            return ResponseEntity.ok(creditCard.getId());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint to get all credit cards of a user.
     *
     * @param userId The ID of the user.
     * @return A list of credit cards associated with the user.
     */
    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<CreditCard> creditCards = user.getCreditCardList();
            List<CreditCardView> creditCardViewList = creditCards.stream().map(creditCard -> new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber())).collect(Collectors.toList());
            return ResponseEntity.ok(creditCardViewList);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint to get the user ID associated with a credit card.
     *
     * @param creditCardNumber The number of the credit card.
     * @return The ID of the user associated with the credit card.
     */
    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(creditCardNumber);
        return creditCardOptional.map(creditCard -> ResponseEntity.ok(creditCard.getOwner().getId())).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    /**
     * Endpoint to update the balance of a credit card.
     *
     * @param payloadList The list of payloads containing the credit card number and the new balance.
     * @return A response entity indicating the result of the operation.
     */
    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Void> updateCreditCardBalance(@RequestBody UpdateBalancePayload[] payloadList) {

        for (UpdateBalancePayload payload : payloadList) {
            String creditCardNumber = payload.getCreditCardNumber();

            Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(creditCardNumber);

            if (creditCardOptional.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CreditCard creditCard = creditCardOptional.get();
            List<BalanceHistory> balanceHistory = creditCard.getBalanceHistory();

            fillBalanceHistoryGaps(balanceHistory);

            updateFollowingDatesBalance(payload, balanceHistory);

            creditCardRepository.save(creditCard);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Method to fill the gaps in the balance history of a credit card.
     *
     * @param balanceHistory The list of balance history entries.
     */
    private void fillBalanceHistoryGaps(List<BalanceHistory> balanceHistory) {
        if (balanceHistory.isEmpty()) {
            return;
        }

        LocalDate latestDate = balanceHistory.get(0).getDate();
        CreditCard creditCard = balanceHistory.get(0).getCreditCard();
        LocalDate earliestDate = balanceHistory.get(balanceHistory.size() - 1).getDate();

        double currentBalance = balanceHistory.get(0).getBalance();
        LocalDate currentDate = earliestDate;
        int currentIndex = balanceHistory.size() - 1;

        while (currentDate.isBefore(latestDate.plusDays(1))) {
            // Check if the current date matches the date in the list
            if (currentDate.equals(balanceHistory.get(currentIndex).getDate())) {
                // Update current balance if necessary
                currentBalance = balanceHistory.get(currentIndex).getBalance();
                currentIndex--;
            } else {
                // Date gap found, insert a new entry with previous day's balance
                BalanceHistory newBalanceHistory = new BalanceHistory();
                newBalanceHistory.setBalance(currentBalance);
                newBalanceHistory.setDate(currentDate);
                newBalanceHistory.setCreditCard(creditCard);
                balanceHistory.add(newBalanceHistory);
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    /**
     * Method to update the balance of a credit card for all dates following a specific date.
     *
     * @param payload        The payload containing the credit card number and the new balance.
     * @param balanceHistory The list of balance history entries.
     */
    private void updateFollowingDatesBalance(UpdateBalancePayload payload, List<BalanceHistory> balanceHistory) {
        double difference = 0.0;

        for (int i = balanceHistory.size() - 1; i >= 0; i--) {
            BalanceHistory balanceHistoryEntry = balanceHistory.get(i);
            if (balanceHistoryEntry.getDate().isEqual(payload.getBalanceDate())) {
                difference = payload.getBalanceAmount() - balanceHistoryEntry.getBalance();
                balanceHistoryEntry.setBalance(balanceHistoryEntry.getBalance() + difference);
            }
            if (balanceHistoryEntry.getDate().isAfter(payload.getBalanceDate())) {
                balanceHistoryEntry.setBalance(balanceHistoryEntry.getBalance() + difference);
            }
        }
    }
}