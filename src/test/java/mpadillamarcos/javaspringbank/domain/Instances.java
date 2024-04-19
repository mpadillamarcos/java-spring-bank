package mpadillamarcos.javaspringbank.domain;

import mpadillamarcos.javaspringbank.domain.balance.Balance;
import mpadillamarcos.javaspringbank.domain.transaction.Transaction;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionRequest;

import static java.time.Instant.now;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.AccountAccessBuilder;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.newAccountAccess;
import static mpadillamarcos.javaspringbank.domain.account.Account.AccountBuilder;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.balance.Balance.newBalance;
import static mpadillamarcos.javaspringbank.domain.money.Money.eur;
import static mpadillamarcos.javaspringbank.domain.transaction.Transaction.newTransaction;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;

public class Instances {

    public static AccountBuilder dummyAccount() {
        return newAccount()
                .createdDate(now())
                .userId(randomUserId());
    }

    public static AccountAccessBuilder dummyAccountAccess() {
        return newAccountAccess()
                .accountId(randomAccountId())
                .type(OWNER)
                .createdDate(now())
                .userId(randomUserId());
    }

    public static Balance.BalanceBuilder dummyBalance() {
        return newBalance()
                .accountId(randomAccountId());
    }

    public static TransactionRequest dummyTransferRequest() {
        return TransactionRequest.builder()
                .userId(randomUserId())
                .originAccountId(randomAccountId())
                .destinationAccountId(randomAccountId())
                .amount(eur(100))
                .type(TRANSFER)
                .build();
    }

    public static Transaction.TransactionBuilder dummyTransfer() {
        return newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(100d))
                .createdDate(now())
                .type(TRANSFER)
                .direction(OUTGOING);
    }
}
