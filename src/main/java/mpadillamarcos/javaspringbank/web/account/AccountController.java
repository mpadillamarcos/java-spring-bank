package mpadillamarcos.javaspringbank.web.account;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.Account;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/accounts")
public class AccountController {

    private final AccountService service;

    @PostMapping
    public AccountDto openAccount(@PathVariable UUID userId) {
        return toDto(service.openAccount(userId(userId)));
    }

    @GetMapping
    public List<AccountDto> listUserAccounts(@PathVariable UUID userId) {
        return service.listUserAccounts(userId(userId))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/{accountId}")
    public AccountDto findUserAccount(@PathVariable UUID userId, @PathVariable UUID accountId) {
        return service.findUserAccount(userId(userId), accountId(accountId))
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("account not found"));
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .id(account.getId().value())
                .userId(account.getUserId().value())
                .state(account.getState())
                .createdDate(account.getCreatedDate())
                .build();
    }
}
