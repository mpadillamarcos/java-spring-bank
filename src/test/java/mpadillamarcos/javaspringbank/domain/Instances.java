package mpadillamarcos.javaspringbank.domain;

import static java.time.Instant.now;
import static mpadillamarcos.javaspringbank.domain.account.Account.AccountBuilder;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;

public class Instances {

    public static AccountBuilder dummyAccount() {
        return newAccount()
                .createdDate(now())
                .userId(randomUserId());
    }
}
