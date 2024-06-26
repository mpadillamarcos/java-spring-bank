package mpadillamarcos.javaspringbank.domain.account;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.balance.Balance;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.time.Clock;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.OWNER;
import static mpadillamarcos.javaspringbank.domain.account.Account.newAccount;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final BalanceService balanceService;
    private final AccountAccessService accessService;
    private final AccountRepository repository;
    private final Clock clock;

    @Transactional
    public AccountView openAccount(UserId userId) {
        var account = newAccount()
                .userId(userId)
                .createdDate(clock.now())
                .build();

        repository.insert(account);

        var access = accessService.grantAccess(account.getId(), userId, OWNER);
        var balance = balanceService.createBalance(account.getId());

        return new AccountView(account, access, balance);
    }

    public List<AccountView> listUserAccounts(UserId userId) {
        var accesses = accessService.listAllAccountAccesses(userId).stream()
                .collect(toMap(AccountAccess::getAccountId, identity()));
        var accountIds = accesses.keySet();
        var balances = balanceService.getBalances(accountIds).stream()
                .collect(toMap(Balance::getAccountId, identity()));
        var accounts = repository.getAccounts(accountIds);

        return accounts.stream()
                .map(account -> new AccountView(account, accesses.get(account.getId()), balances.get(account.getId())))
                .sorted(comparing(AccountView::getCreatedDate))
                .toList();
    }

    public Optional<AccountView> findAccountView(UserId userId, AccountId accountId) {
        var access = accessService.findAccountAccess(accountId, userId).orElseThrow(this::accountNotFound);
        var account = repository.findById(accountId).orElseThrow(this::accountNotFound);
        var balance = balanceService.getBalance(accountId);

        return Optional.of(new AccountView(account, access, balance));
    }

    public Account getById(AccountId accountId) {
        return repository.findById(accountId)
                .orElseThrow(this::accountNotFound);
    }

    @Transactional
    public void blockAccount(AccountId accountId) {
        var account = getById(accountId);

        repository.update(account.block());
    }

    @Transactional
    public void unblockAccount(AccountId accountId) {
        var account = getById(accountId);

        repository.update(account.unblock());
    }

    @Transactional
    public void closeAccount(AccountId accountId) {
        var account = getById(accountId);

        repository.update(account.close());
    }

    private NotFoundException accountNotFound() {
        return new NotFoundException("account not found");
    }
}
