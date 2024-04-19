package mpadillamarcos.javaspringbank.web.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionRequest;
import mpadillamarcos.javaspringbank.domain.transaction.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.TRANSFER;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.WITHDRAW;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/accounts/{accountId}")
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/transfers")
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

    @PostMapping("/withdrawals")
    public void withdraw(
            @PathVariable UUID userId,
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawRequest request) {
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
}
