package mpadillamarcos.javaspringbank.domain;

import mpadillamarcos.javaspringbank.domain.balance.Balance;

import static java.time.Instant.now;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.AccountAccessBuilder;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.newAccountAccess;
import static mpadillamarcos.javaspringbank.domain.account.Account.AccountBuilder;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.balance.Balance.newBalance;
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
}
