package mpadillamarcos.javaspringbank.web.account;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.Account;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping("/users/{userId}/accounts")
    public AccountDto openAccount(@PathVariable UUID userId) {
        var account = service.openAccount(userId(userId));

        return toDto(account);
    }

    @GetMapping("/users/{userId}/accounts")
    public List<AccountDto> listUserAccounts(@PathVariable UUID userId) {
        return service.listUserAccounts(userId(userId))
                .stream()
                .map(this::toDto)
                .toList();
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
