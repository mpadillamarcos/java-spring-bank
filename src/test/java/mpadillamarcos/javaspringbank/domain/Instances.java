package mpadillamarcos.javaspringbank.domain;

import mpadillamarcos.javaspringbank.domain.balance.Balance;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.transaction.Transaction;

import static java.time.Instant.now;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.AccountAccessBuilder;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.newAccountAccess;
import static mpadillamarcos.javaspringbank.domain.account.Account.AccountBuilder;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.balance.Balance.newBalance;
import static mpadillamarcos.javaspringbank.domain.transaction.Transaction.newTransaction;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.OUTGOING;
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

    public static Transaction.TransactionBuilder dummyTransaction() {
        return newTransaction()
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(Money.eur(100d))
                .createdDate(now())
                .type(OUTGOING);
    }
}
