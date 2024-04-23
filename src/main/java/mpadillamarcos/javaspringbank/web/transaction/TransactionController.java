package mpadillamarcos.javaspringbank.web.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.transaction.DepositRequest;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import mpadillamarcos.javaspringbank.domain.transaction.TransferRequest;
import mpadillamarcos.javaspringbank.domain.transaction.WithdrawRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.transactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.TRANSFER;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/users/{userId}/accounts/{accountId}/transfer")
    public void createTransfer(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody CreateTransferRequest request) {
        service.transfer(
                TransferRequest.transferRequestBuilder()
                        .amount(request.getAmount())
                        .destinationAccountId(accountId(request.getDestinationAccountId()))
                        .originAccountId(accountId(accountId))
                        .userId(userId(userId))
                        .type(TRANSFER)
                        .concept(request.getConcept())
                        .build()
        );
    }

    @PostMapping("/users/{userId}/accounts/{accountId}/withdrawal")
    public void withdraw(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawOrDepositRequest request) {
        service.withdraw(
                WithdrawRequest.withdrawRequestBuilder()
                        .amount(request.getAmount())
                        .accountId(accountId(accountId))
                        .userId(userId(userId))
//                        .type(WITHDRAW)
                        .concept(request.getConcept())
                        .build()
        );
    }

    @PostMapping("/users/{userId}/accounts/{accountId}/deposit")
    public void deposit(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawOrDepositRequest request) {
        service.deposit(
                DepositRequest.depositRequestBuilder()
                        .amount(request.getAmount())
                        .accountId(accountId(accountId))
                        .userId(userId(userId))
//                        .type(DEPOSIT)
                        .concept(request.getConcept())
                        .build()
        );
    }

    @PostMapping("/transactions/{transactionId}/confirm")
    public void confirm(@PathVariable UUID transactionId) {
        service.confirmTransaction(transactionId(transactionId));
    }
}
