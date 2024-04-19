package mpadillamarcos.javaspringbank.web.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionRequest;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/accounts/{originAccountId}/transactions")
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public void createTransaction(
            @PathVariable UUID userId,
            @PathVariable UUID originAccountId,
            @Valid @RequestBody CreateTransactionRequest request) {
        service.createTransaction(
                TransactionRequest.builder()
                        .amount(request.getAmount())
                        .destinationAccountId(accountId(request.getDestinationAccountId()))
                        .originAccountId(accountId(originAccountId))
                        .userId(userId(userId))
                        .build());
    }
}
