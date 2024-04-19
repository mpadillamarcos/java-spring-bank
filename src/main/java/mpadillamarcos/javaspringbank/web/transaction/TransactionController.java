package mpadillamarcos.javaspringbank.web.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionRequest;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.transactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/users/{userId}/accounts/{accountId}/transfers")
    public void createTransfer(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody CreateTransferRequest request) {
        service.createTransfer(
                TransactionRequest.builder()
                        .amount(request.getAmount())
                        .destinationAccountId(accountId(request.getDestinationAccountId()))
                        .originAccountId(accountId(accountId))
                        .userId(userId(userId))
                        .type(TRANSFER)
                        .concept(request.getConcept())
                        .build()
        );
    }

    @PostMapping("/users/{userId}/accounts/{accountId}/withdrawals")
    public void withdraw(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawOrDepositRequest request) {
        service.withdraw(
                TransactionRequest.builder()
                        .amount(request.getAmount())
                        .originAccountId(accountId(accountId))
                        .userId(userId(userId))
                        .type(WITHDRAW)
                        .concept(request.getConcept())
                        .build()
        );
    }

    @PostMapping("/users/{userId}/accounts/{accountId}/deposits")
    public void deposit(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawOrDepositRequest request) {
        service.deposit(
                TransactionRequest.builder()
                        .amount(request.getAmount())
                        .originAccountId(accountId(accountId))
                        .userId(userId(userId))
                        .type(DEPOSIT)
                        .concept(request.getConcept())
                        .build()
        );
    }

    @PostMapping("/transactions/{transactionId}/confirm")
    public void confirm(@PathVariable UUID transactionId) {
        service.confirmTransaction(transactionId(transactionId));
    }
}
