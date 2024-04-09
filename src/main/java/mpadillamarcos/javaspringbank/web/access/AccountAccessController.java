package mpadillamarcos.javaspringbank.web.access;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static mpadillamarcos.javaspringbank.domain.account.AccountId.accountId;
import static mpadillamarcos.javaspringbank.domain.user.UserId.userId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/accounts/{accountId}/access")
public class AccountAccessController {

    private final AccountAccessService service;

    @PostMapping
    public void grantAccess(
            @PathVariable UUID accountId,
            @PathVariable UUID userId,
            @Valid @RequestBody GrantAccountAccessRequest request) {
        service.grantAccess(accountId(accountId), userId(userId), request.getType());
    }
}
