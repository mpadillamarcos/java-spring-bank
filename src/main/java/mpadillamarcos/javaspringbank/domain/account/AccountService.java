package mpadillamarcos.javaspringbank.domain.account;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessRepository;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import mpadillamarcos.javaspringbank.infra.account.InMemoryAccountRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountAccessService accessService;
    private final AccountRepository repository;
    private final Clock clock;

    public Account openAccount(UserId userId) {
        var account = newAccount()
                .userId(userId)
                .createdDate(clock.now())
                .build();

        repository.insert(account);
        accessService.grantAccess(account.getId(), userId, OWNER);

        return account;
    }

    public List<AccountView> listUserAccounts(UserId userId) {
        var accesses = accessService.listAllAccountAccesses(userId).stream()
                .collect(toMap(AccountAccess::getAccountId, identity()));
        var accountIds = accesses.keySet();
        var accounts = repository.getAccounts(accountIds);

        return accounts.stream()
                .map(account -> new AccountView(account, accesses.get(account.getId())))
                .sorted(comparing(AccountView::getCreatedDate))
                .toList();
    }

    public Optional<Account> findUserAccount(UserId userId, AccountId accountId) {
        return repository.findUserAccount(userId, accountId);
    }

    public void blockUserAccount(UserId userId, AccountId accountId) {
        var account = getUserAccount(userId, accountId);

        repository.update(account.block());
    }

    public void reopenUserAccount(UserId userId, AccountId accountId) {
        var account = getUserAccount(userId, accountId);

        repository.update(account.reopen());
    }

    public void closeUserAccount(UserId userId, AccountId accountId) {
        var account = getUserAccount(userId, accountId);

        repository.update(account.close());
    }

    private Account getUserAccount(UserId userId, AccountId accountId) {
        return repository.findUserAccount(userId, accountId)
                .orElseThrow(() -> new NotFoundException("account not found"));
    }

}
