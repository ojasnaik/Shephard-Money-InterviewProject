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


@RestController
public class CreditCardController {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

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

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(creditCardNumber);
        return creditCardOptional.map(creditCard -> ResponseEntity.ok(creditCard.getOwner().getId())).orElseGet(() -> ResponseEntity.badRequest().build());
    }

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

    private void updateFollowingDatesBalance(UpdateBalancePayload payload, List<BalanceHistory> balanceHistory) {
        double difference = 0.0;

        for (int i = balanceHistory.size() - 1; i >= 0; i--) {
            BalanceHistory balanceHistoryEntry = balanceHistory.get(i);
            if (balanceHistoryEntry.getDate().isEqual(payload.getBalanceDate())) {
                difference = payload.getBalanceAmount() - balanceHistoryEntry.getBalance();
            }
            balanceHistoryEntry.setBalance(balanceHistoryEntry.getBalance() + difference);
        }
    }

}
