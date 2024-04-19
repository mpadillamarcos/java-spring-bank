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
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.CONFIRMED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;
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
    class CreateTransfer {

        @Test
        void throws_exception_when_user_has_no_access_to_origin_account() {
            var request = dummyTransferRequest();

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(empty());

            assertThrows(AccessDeniedException.class, () -> service.createTransfer(request));
        }

        @Test
        void throws_exception_when_user_access_is_revoked() {
            var request = dummyTransferRequest();
            var access = access(request, REVOKED, OPERATOR);

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.createTransfer(request));
        }

        @Test
        void throws_exception_when_user_type_is_viewer() {
            var request = dummyTransferRequest();
            var access = access(request, GRANTED, VIEWER);

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.createTransfer(request));
        }

        @Test
        void throws_exception_when_origin_account_state_is_not_open() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var accountId = request.getOriginAccountId();
            var access = access(request);
            var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(TransactionNotAllowedException.class, () -> service.createTransfer(request));
        }

        @Test
        void throws_not_found_exception_when_destination_account_does_not_exist() {
            var request = dummyTransferRequest();
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

            assertThrows(NotFoundException.class, () -> service.createTransfer(request));
        }

        @Test
        void throws_exception_when_destination_account_state_is_not_open() {
            var request = dummyTransferRequest();
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

            assertThrows(TransactionNotAllowedException.class, () -> service.createTransfer(request));
        }

        @Test
        void updates_origin_account_balance() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var destinationAccount = dummyAccount().id(destinationAccountId).build();

            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(true);
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.createTransfer(request);

            verify(balanceService, times(1))
                    .withdraw(originAccountId, request.getAmount());
        }

        @Test
        void creates_new_transaction_for_the_origin_account_user() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransfer()
                    .accountId(originAccountId)
                    .userId(userId)
                    .type(TRANSFER)
                    .build();

            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(true);
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.createTransfer(request);

            assertThat(repository.findLastTransactionByAccountId(originAccountId))
                    .get()
                    .returns(originAccountId, Transaction::getAccountId)
                    .returns(userId, Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(newTransaction.getState(), Transaction::getState)
                    .returns(newTransaction.getType(), Transaction::getType)
                    .returns(newTransaction.getDirection(), Transaction::getDirection)
                    .returns(newTransaction.getConcept(), Transaction::getConcept);
        }

        @Test
        void creates_new_transaction_for_the_destination_account_user() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var access = access(request);
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransfer()
                    .accountId(destinationAccountId)
                    .userId(userId)
                    .type(TRANSFER)
                    .direction(INCOMING)
                    .build();

            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accountService.exists(destinationAccountId))
                    .thenReturn(true);
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.createTransfer(request);

            assertThat(repository.findLastTransactionByAccountId(destinationAccountId))
                    .get()
                    .returns(destinationAccountId, Transaction::getAccountId)
                    .returns(userId, Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(newTransaction.getState(), Transaction::getState)
                    .returns(newTransaction.getType(), Transaction::getType)
                    .returns(newTransaction.getDirection(), Transaction::getDirection)
                    .returns(newTransaction.getConcept(), Transaction::getConcept);
        }
    }

    @Nested
    class Withdraw {

        @Test
        void throws_exception_when_user_has_no_access_to_account() {
            var request = dummyWithdrawRequest();

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(empty());

            assertThrows(AccessDeniedException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_user_access_is_revoked() {
            var request = dummyWithdrawRequest();
            var access = access(request, REVOKED, OPERATOR);

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_user_type_is_viewer() {
            var request = dummyWithdrawRequest();
            var access = access(request, GRANTED, VIEWER);

            when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getOriginAccountId();
            var access = access(request);
            var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(TransactionNotAllowedException.class, () -> service.withdraw(request));
        }

        @Test
        void updates_account_balance() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getOriginAccountId();
            var access = access(request);
            var account = dummyAccount().userId(userId).id(accountId).build();

            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(accountId))
                    .thenReturn(account);

            service.withdraw(request);

            verify(balanceService, times(1))
                    .withdraw(accountId, request.getAmount());
        }

        @Test
        void creates_new_withdrawal() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getOriginAccountId();
            var access = access(request);
            var account = dummyAccount().userId(userId).id(accountId).build();
            var newTransaction = dummyWithdraw()
                    .accountId(accountId)
                    .userId(userId)
                    .type(WITHDRAW)
                    .state(CONFIRMED)
                    .build();

            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(accountId))
                    .thenReturn(account);

            service.withdraw(request);

            assertThat(repository.findLastTransactionByAccountId(accountId))
                    .get()
                    .returns(accountId, Transaction::getAccountId)
                    .returns(userId, Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(newTransaction.getState(), Transaction::getState)
                    .returns(newTransaction.getType(), Transaction::getType)
                    .returns(newTransaction.getDirection(), Transaction::getDirection)
                    .returns(newTransaction.getConcept(), Transaction::getConcept);
        }

        @Nested
        class Deposit {

            @Test
            void throws_exception_when_user_has_no_access_to_account() {
                var request = dummyDepositRequest();

                when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                        .thenReturn(empty());

                assertThrows(AccessDeniedException.class, () -> service.deposit(request));
            }

            @Test
            void throws_exception_when_user_access_is_revoked() {
                var request = dummyDepositRequest();
                var access = access(request, REVOKED, OPERATOR);

                when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                        .thenReturn(Optional.of(access));

                assertThrows(AccessDeniedException.class, () -> service.deposit(request));
            }

            @Test
            void throws_exception_when_user_type_is_viewer() {
                var request = dummyDepositRequest();
                var access = access(request, GRANTED, VIEWER);

                when(accessService.findAccountAccess(request.getOriginAccountId(), request.getUserId()))
                        .thenReturn(Optional.of(access));

                assertThrows(AccessDeniedException.class, () -> service.deposit(request));
            }

            @Test
            void throws_exception_when_account_state_is_not_open() {
                var request = dummyDepositRequest();
                var userId = request.getUserId();
                var accountId = request.getOriginAccountId();
                var access = access(request);
                var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

                when(accessService.findAccountAccess(accountId, userId))
                        .thenReturn(Optional.of(access));
                when(accountService.getById(accountId))
                        .thenReturn(account);

                assertThrows(TransactionNotAllowedException.class, () -> service.deposit(request));
            }

            @Test
            void updates_account_balance() {
                var request = dummyDepositRequest();
                var userId = request.getUserId();
                var accountId = request.getOriginAccountId();
                var access = access(request);
                var account = dummyAccount().userId(userId).id(accountId).build();

                when(accessService.findAccountAccess(accountId, userId))
                        .thenReturn(Optional.of(access));
                when(accountService.getById(accountId))
                        .thenReturn(account);

                service.deposit(request);

                verify(balanceService, times(1))
                        .deposit(accountId, request.getAmount());
            }

            @Test
            void creates_new_deposit() {
                var request = dummyDepositRequest();
                var userId = request.getUserId();
                var accountId = request.getOriginAccountId();
                var access = access(request);
                var account = dummyAccount().userId(userId).id(accountId).build();
                var newTransaction = dummyDeposit()
                        .accountId(accountId)
                        .userId(userId)
                        .type(DEPOSIT)
                        .state(CONFIRMED)
                        .build();

                when(accessService.findAccountAccess(accountId, userId))
                        .thenReturn(Optional.of(access));
                when(accountService.getById(accountId))
                        .thenReturn(account);

                service.deposit(request);

                assertThat(repository.findLastTransactionByAccountId(accountId))
                        .get()
                        .returns(accountId, Transaction::getAccountId)
                        .returns(userId, Transaction::getUserId)
                        .returns(newTransaction.getAmount(), Transaction::getAmount)
                        .returns(NOW, Transaction::getCreatedDate)
                        .returns(newTransaction.getState(), Transaction::getState)
                        .returns(newTransaction.getType(), Transaction::getType)
                        .returns(newTransaction.getDirection(), Transaction::getDirection)
                        .returns(newTransaction.getConcept(), Transaction::getConcept);
            }
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