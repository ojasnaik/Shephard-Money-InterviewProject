package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
//index for fast retrieval
@Table(name = "balance_history",
        indexes = {@Index(name = "index_on_date", columnList = "date"),
                @Index(name = "index_on_date_and_creditCardId", columnList = "date,credit_card_id")})
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private LocalDate date;

    private double balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;
}