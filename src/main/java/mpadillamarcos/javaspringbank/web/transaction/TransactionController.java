package mpadillamarcos.javaspringbank.web.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.transaction.Transaction;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.transaction.DepositRequest.depositRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.transactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransferRequest.transferRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.WithdrawRequest.withdrawRequest;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/users/{userId}/accounts/{accountId}/transfer")
    public void transfer(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody TransferRequest request) {
        service.transfer(transferRequest()
                .originAccountId(accountId(accountId))
                .userId(userId(userId))
                .destinationAccountId(accountId(request.getDestinationAccountId()))
                .amount(request.getAmount())
                .concept(request.getConcept())
                .build());
    }

    @PostMapping("/users/{userId}/accounts/{accountId}/withdraw")
    public void withdraw(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawRequest request) {
        service.withdraw(withdrawRequest()
                .accountId(accountId(accountId))
                .userId(userId(userId))
                .amount(request.getAmount())
                .concept(request.getConcept())
                .build());
    }

    @PostMapping("/users/{userId}/accounts/{accountId}/deposit")
    public void deposit(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody DepositRequest request) {
        service.deposit(depositRequest()
                .accountId(accountId(accountId))
                .userId(userId(userId))
                .amount(request.getAmount())
                .concept(request.getConcept())
                .build());
    }

    @PostMapping("/transactions/{transactionId}/confirm")
    public void confirm(@PathVariable UUID transactionId) {
        service.confirm(transactionId(transactionId));
    }

    @PostMapping("/transactions/{transactionId}/reject")
    public void reject(@PathVariable UUID transactionId) {
        service.reject(transactionId(transactionId));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<TransactionDto> listTransactions(@PathVariable UUID accountId) {
        return service.listTransactionsByAccountId(accountId(accountId))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private TransactionDto toDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId().value())
                .userId(transaction.getUserId().value())
                .accountId(transaction.getAccountId().value())
                .amount(transaction.getAmount())
                .createdDate(transaction.getCreatedDate())
                .state(transaction.getState())
                .direction(transaction.getDirection())
                .type(transaction.getType())
                .concept(transaction.getConcept())
                .build();
    }

}
