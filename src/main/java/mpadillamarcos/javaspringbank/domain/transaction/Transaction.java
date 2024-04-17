package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.Builder;
import lombok.Value;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;

import java.time.Instant;

import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.randomTransactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.PENDING;
import static mpadillamarcos.javaspringbank.utils.Checks.require;

@Builder(toBuilder = true)
@Value
public class Transaction {

    TransactionId id;
    TransactionGroupId groupId;
    UserId userId;
    AccountId accountId;
    Money amount;
    Instant createdDate;
    TransactionState state;
    TransactionType type;

    public Transaction(
            TransactionId id,
            TransactionGroupId groupId,
            UserId userId,
            AccountId accountId,
            Money amount,
            Instant createdDate,
            TransactionState state,
            TransactionType type
    ) {
        this.id = require("id", id);
        this.groupId = require("groupId", groupId);
        this.userId = require("userId", userId);
        this.accountId = require("accountId", accountId);
        this.amount = require("amount", amount);
        this.createdDate = require("createdDate", createdDate);
        this.state = require("state", state);
        this.type = require("type", type);
    }

    public static TransactionBuilder newTransaction() {
        return builder()
                .id(randomTransactionId())
                .groupId(randomTransactionGroupId())
                .state(PENDING);
    }

}
