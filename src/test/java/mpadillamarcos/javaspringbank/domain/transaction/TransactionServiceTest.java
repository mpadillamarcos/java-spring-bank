package mpadillamarcos.javaspringbank.domain.transaction;

import mpadillamarcos.javaspringbank.domain.access.AccessState;
import mpadillamarcos.javaspringbank.domain.access.AccessType;
import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.AccessDeniedException;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.exception.TransactionNotAllowedException;
import mpadillamarcos.javaspringbank.infra.TestClock;
import mpadillamarcos.javaspringbank.infra.transaction.InMemoryTransactionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static mpadillamarcos.javaspringbank.domain.Instances.*;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.GRANTED;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.*;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.BLOCKED;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.CLOSED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.INCOMING;
import static mpadillamarcos.javaspringbank.infra.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private final TransactionRepository repository = new InMemoryTransactionRepository();
    private final AccountService accountService = mock(AccountService.class);
    private final AccountAccessService accessService = mock(AccountAccessService.class);
    private final BalanceService balanceService = mock(BalanceService.class);
    private final TransactionService service = new TransactionService(
            repository, accountService, accessService, balanceService, new TestClock()
    );

    @Nested
    class CreateTransaction {

        @Test
        void throws_exception_when_user_has_no_access_to_origin_account() {
            var request = dummyTransactionRequest();

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(empty());

            assertThrows(AccessDeniedException.class, () -> service.createTransaction(request));
        }

        @Test
        void throws_exception_when_user_access_is_revoked() {
            var request = dummyTransactionRequest();
            var access = access(request, REVOKED, OPERATOR);

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.createTransaction(request));
        }

        @Test
        void throws_exception_when_user_type_is_viewer() {
            var request = dummyTransactionRequest();
            var access = access(request, GRANTED, VIEWER);

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.createTransaction(request));
        }

        @Test
        void throws_exception_when_origin_account_state_is_not_open() {
            var request = dummyTransactionRequest();
            var userId = request.getUserId();
            var accountId = request.getOriginAccountId();
            var access = access(request);
            var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(TransactionNotAllowedException.class, () -> service.createTransaction(request));
        }

        @Test
        void throws_not_found_exception_when_destination_account_does_not_exist() {
            var request = dummyTransactionRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();

            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(false);

            assertThrows(NotFoundException.class, () -> service.createTransaction(request));
        }

        @Test
        void throws_exception_when_destination_account_state_is_not_open() {
            var request = dummyTransactionRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var destinationAccount = dummyAccount().id(destinationAccountId).state(CLOSED).build();

            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(true);
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            assertThrows(TransactionNotAllowedException.class, () -> service.createTransaction(request));
        }

        @Test
        void updates_origin_account_balance() {
            var request = dummyTransactionRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var destinationAccount = dummyAccount().id(destinationAccountId).build();

            when(accessService.findAccountAccess(originAccountId, request.getUserId()))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(true);
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.createTransaction(request);

            verify(balanceService, times(1))
                    .withdraw(originAccountId, request.getAmount());
        }

        @Test
        void creates_new_transaction_for_the_origin_account_user() {
            var request = dummyTransactionRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransaction().accountId(originAccountId).userId(request.getUserId()).build();

            when(accessService.findAccountAccess(originAccountId, request.getUserId()))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(true);
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.createTransaction(request);

            assertThat(repository.findLastTransactionByAccountId(originAccountId))
                    .get()
                    .returns(originAccountId, Transaction::getAccountId)
                    .returns(newTransaction.getUserId(), Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(newTransaction.getState(), Transaction::getState)
                    .returns(newTransaction.getType(), Transaction::getType);
        }

        @Test
        void creates_new_transaction_for_the_destination_account_user() {
            var request = dummyTransactionRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransaction()
                    .accountId(destinationAccountId)
                    .userId(request.getUserId())
                    .type(INCOMING)
                    .build();

            when(accessService.findAccountAccess(originAccountId, request.getUserId()))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(true);
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.createTransaction(request);

            assertThat(repository.findLastTransactionByAccountId(destinationAccountId))
                    .get()
                    .returns(destinationAccountId, Transaction::getAccountId)
                    .returns(newTransaction.getUserId(), Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(newTransaction.getState(), Transaction::getState)
                    .returns(newTransaction.getType(), Transaction::getType);
        }
    }

    private static AccountAccess access(TransactionRequest request) {
        return access(request, GRANTED, OWNER);
    }

    private static AccountAccess access(TransactionRequest request, AccessState state, AccessType type) {
        return dummyAccountAccess()
                .userId(request.getUserId())
                .accountId(request.getOriginAccountId())
                .state(state)
                .type(type)
                .build();
    }
}