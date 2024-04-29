package mpadillamarcos.javaspringbank.domain;

import mpadillamarcos.javaspringbank.domain.balance.Balance;
import mpadillamarcos.javaspringbank.domain.transaction.DepositRequest;
import mpadillamarcos.javaspringbank.domain.transaction.Transaction;
import mpadillamarcos.javaspringbank.domain.transaction.TransferRequest;
import mpadillamarcos.javaspringbank.domain.transaction.WithdrawRequest;

import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.AccountAccessBuilder;
import static mpadillamarcos.javaspringbank.domain.access.AccountAccess.newAccountAccess;
import static mpadillamarcos.javaspringbank.domain.account.Account.AccountBuilder;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.balance.Balance.newBalance;
import static mpadillamarcos.javaspringbank.domain.money.Money.eur;
import static mpadillamarcos.javaspringbank.domain.transaction.DepositRequest.depositRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.Transaction.newTransaction;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;
import static mpadillamarcos.javaspringbank.domain.transaction.TransferRequest.transferRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.WithdrawRequest.withdrawRequest;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;

public class Instances {

    public static AccountBuilder dummyAccount() {
        return newAccount()
                .createdDate(NOW)
                .userId(randomUserId());
    }

    public static AccountAccessBuilder dummyAccountAccess() {
        return newAccountAccess()
                .accountId(randomAccountId())
                .type(OWNER)
                .createdDate(NOW)
                .userId(randomUserId());
    }

    public static Balance.BalanceBuilder dummyBalance() {
        return newBalance()
                .accountId(randomAccountId());
    }

    public static TransferRequest dummyTransferRequest() {
        return transferRequest()
                .userId(randomUserId())
                .originAccountId(randomAccountId())
                .destinationAccountId(randomAccountId())
                .amount(eur(100))
                .build();
    }

    public static TransferRequest.TransferRequestBuilder dummyTransferRequestBuilder() {
        return transferRequest()
                .userId(randomUserId())
                .originAccountId(randomAccountId())
                .destinationAccountId(randomAccountId())
                .amount(eur(100));
    }

    public static Transaction.TransactionBuilder dummyTransfer() {
        return newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(100))
                .createdDate(NOW)
                .type(TRANSFER)
                .direction(OUTGOING);
    }

    public static WithdrawRequest dummyWithdrawRequest() {
        return withdrawRequest()
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(50))
                .build();
    }

    public static WithdrawRequest.WithdrawRequestBuilder dummyWithdrawRequestBuilder() {
        return withdrawRequest()
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(50));
    }

    public static Transaction.TransactionBuilder dummyWithdraw() {
        return newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(50))
                .createdDate(NOW)
                .type(WITHDRAW)
                .direction(OUTGOING);
    }

    public static DepositRequest dummyDepositRequest() {
        return depositRequest()
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(50))
                .build();
    }

    public static DepositRequest.DepositRequestBuilder dummyDepositRequestBuilder() {
        return depositRequest()
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(50));
    }

    public static Transaction.TransactionBuilder dummyDeposit() {
        return newTransaction()
                .groupId(randomTransactionGroupId())
                .userId(randomUserId())
                .accountId(randomAccountId())
                .amount(eur(50))
                .createdDate(NOW)
                .type(DEPOSIT)
                .direction(INCOMING);
    }
}
