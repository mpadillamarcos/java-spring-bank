package mpadillamarcos.javaspringbank.web.account;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.account.AccountView;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping("/users/{userId}/accounts")
    public AccountViewDto openAccount(@PathVariable UUID userId) {
        return toDto(service.openAccount(userId(userId)));
    }

    @GetMapping("/users/{userId}/accounts")
    public List<AccountViewDto> listUserAccounts(@PathVariable UUID userId) {
        return service.listUserAccounts(userId(userId)).stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/users/{userId}/accounts/{accountId}")
    public AccountViewDto findAccount(@PathVariable UUID userId, @PathVariable UUID accountId) {
        Optional<AccountView> accountView = service.findAccountView(userId(userId), accountId(accountId));
        if (accountView.isEmpty()) {
            throw new NotFoundException("account not found");
        }
        return toDto(accountView.get());
    }

    @PostMapping("accounts/{accountId}/block")
    public void blockAccount(@PathVariable UUID accountId) {
        service.blockAccount(accountId(accountId));
    }

    @PostMapping("accounts/{accountId}/unblock")
    public void unblockAccount(@PathVariable UUID accountId) {
        service.unblockAccount(accountId(accountId));
    }

    @DeleteMapping("accounts/{accountId}")
    public void closeAccount(@PathVariable UUID accountId) {
        service.closeAccount(accountId(accountId));
    }

    private AccountViewDto toDto(AccountView account) {
        return AccountViewDto.builder()
                .id(account.getAccountId().value())
                .userId(account.getUserId().value())
                .state(account.getState())
                .createdDate(account.getCreatedDate())
                .accessType(account.getAccessType())
                .balance(account.getBalance())
                .build();
    }
}
