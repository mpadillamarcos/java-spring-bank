package mpadillamarcos.javaspringbank.domain.transaction;

import mpadillamarcos.javaspringbank.domain.access.AccessState;
import mpadillamarcos.javaspringbank.domain.access.AccessType;
import mpadillamarcos.javaspringbank.domain.access.AccountAccess;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.AccessDeniedException;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.exception.TransactionNotAllowedException;
import mpadillamarcos.javaspringbank.domain.user.UserId;
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
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.BLOCKED;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.CLOSED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.randomTransactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.CONFIRMED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.REJECTED;
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
    class Transfer {

        @Test
        void throws_not_found_when_origin_account_does_not_exist() {
            var request = dummyTransferRequest();
            var originAccountId = request.getOriginAccountId();

            when(accountService.getById(originAccountId))
                    .thenThrow(NotFoundException.class);

            assertThrows(NotFoundException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_origin_account_state_is_not_open() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).state(BLOCKED).build();

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);

            assertThrows(TransactionNotAllowedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_user_has_no_access_to_origin_account() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(empty());

            assertThrows(AccessDeniedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_user_access_is_revoked() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(userId, originAccountId, REVOKED, OPERATOR);

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_user_type_is_viewer() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(userId, originAccountId, GRANTED, VIEWER);

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_not_found_exception_when_destination_account_does_not_exist() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(userId, originAccountId);

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(destinationAccountId))
                    .thenThrow(NotFoundException.class);

            assertThrows(NotFoundException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_destination_account_state_is_not_open() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(userId, originAccountId);
            var destinationAccount = dummyAccount().id(destinationAccountId).state(CLOSED).build();

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            assertThrows(TransactionNotAllowedException.class, () -> service.transfer(request));
        }

        @Test
        void updates_origin_account_balance() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(userId, originAccountId);
            var destinationAccount = dummyAccount().id(destinationAccountId).build();

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.transfer(request);

            verify(balanceService, times(1))
                    .withdraw(originAccountId, request.getAmount());
        }

        @Test
        void creates_new_transaction_for_the_origin_account_user() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getOriginAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(userId, originAccountId);
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransfer()
                    .accountId(originAccountId)
                    .userId(userId)
                    .type(TRANSFER)
                    .build();

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.transfer(request);

            assertThat(repository.findTransactionsByAccountId(originAccountId).getFirst())
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
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(userId, originAccountId);
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransfer()
                    .accountId(destinationAccountId)
                    .userId(userId)
                    .type(TRANSFER)
                    .direction(INCOMING)
                    .build();

            when(accountService.getById(originAccountId))
                    .thenReturn(originAccount);
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.getById(destinationAccountId))
                    .thenReturn(destinationAccount);

            service.transfer(request);

            assertThat(repository.findTransactionsByAccountId(destinationAccountId).getFirst())
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
        void throws_not_found_when_account_does_not_exist() {
            var request = dummyWithdrawRequest();
            var accountId = request.getAccountId();

            when(accountService.getById(accountId))
                    .thenThrow(NotFoundException.class);

            assertThrows(NotFoundException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(TransactionNotAllowedException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_user_has_no_access_to_account() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(empty());

            assertThrows(AccessDeniedException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_user_access_is_revoked() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId, REVOKED, OPERATOR);

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(request.getAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_user_type_is_viewer() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId, GRANTED, VIEWER);

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(request.getAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.withdraw(request));
        }

        @Test
        void creates_new_withdrawal_transaction() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId);
            var newTransaction = dummyWithdraw()
                    .accountId(accountId)
                    .userId(userId)
                    .type(WITHDRAW)
                    .build();

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            service.withdraw(request);

            assertThat(repository.findTransactionsByAccountId(accountId).getFirst())
                    .returns(accountId, Transaction::getAccountId)
                    .returns(userId, Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(CONFIRMED, Transaction::getState)
                    .returns(newTransaction.getType(), Transaction::getType)
                    .returns(newTransaction.getDirection(), Transaction::getDirection)
                    .returns(newTransaction.getConcept(), Transaction::getConcept);
        }

        @Test
        void updates_balance() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId);
            var newTransaction = dummyWithdraw()
                    .accountId(accountId)
                    .userId(userId)
                    .build();

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            service.withdraw(request);

            verify(balanceService, times(1))
                    .withdraw(accountId, newTransaction.getAmount());
        }
    }

    @Nested
    class Deposit {

        @Test
        void throws_not_found_when_account_does_not_exist() {
            var request = dummyDepositRequest();
            var accountId = request.getAccountId();

            when(accountService.getById(accountId))
                    .thenThrow(NotFoundException.class);

            assertThrows(NotFoundException.class, () -> service.deposit(request));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(TransactionNotAllowedException.class, () -> service.deposit(request));
        }

        @Test
        void throws_exception_when_user_has_no_access_to_account() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(empty());

            assertThrows(AccessDeniedException.class, () -> service.deposit(request));
        }

        @Test
        void throws_exception_when_user_access_is_revoked() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId, REVOKED, OPERATOR);

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.deposit(request));
        }

        @Test
        void throws_exception_when_user_type_is_viewer() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId, GRANTED, VIEWER);

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.deposit(request));
        }

        @Test
        void creates_new_deposit_transaction() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId);
            var newTransaction = dummyDeposit()
                    .accountId(accountId)
                    .userId(userId)
                    .type(DEPOSIT)
                    .build();

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            service.deposit(request);

            assertThat(repository.findTransactionsByAccountId(accountId).getFirst())
                    .returns(accountId, Transaction::getAccountId)
                    .returns(userId, Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(CONFIRMED, Transaction::getState)
                    .returns(DEPOSIT, Transaction::getType)
                    .returns(INCOMING, Transaction::getDirection)
                    .returns(newTransaction.getConcept(), Transaction::getConcept);
        }

        @Test
        void updates_balance() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(userId, accountId);
            var newTransaction = dummyDeposit()
                    .accountId(accountId)
                    .userId(userId)
                    .build();

            when(accountService.getById(accountId))
                    .thenReturn(account);
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            service.deposit(request);

            verify(balanceService, times(1))
                    .deposit(accountId, newTransaction.getAmount());
        }
    }

    @Nested
    class ListTransactions {

        @Test
        void returns_nothing_when_account_has_no_transactions() {
            var accountId = randomAccountId();

            assertThat(service.listTransactionsByAccountId(accountId)).isEmpty();
        }

        @Test
        void returns_all_transactions_associated_to_one_account() {
            var accountId = randomAccountId();
            var transaction1 = createTransaction(dummyTransfer().accountId(accountId));
            var transaction2 = createTransaction(dummyDeposit().accountId(accountId));
            var transaction3 = createTransaction(dummyWithdraw().accountId(accountId));

            var transactions = service.listTransactionsByAccountId(accountId);

            assertThat(transactions).hasSize(3);
            assertThat(transactions).containsExactly(transaction1, transaction2, transaction3);
        }
    }

    @Nested
    class Confirm {

        @Test
        void throws_not_found_exception_when_transaction_id_does_not_exist() {
            var transactionId = randomTransactionId();

            assertThrows(NotFoundException.class, () -> service.confirm(transactionId));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var transaction = createTransaction(dummyWithdraw());
            var accountId = transaction.getAccountId();
            var account = dummyAccount().id(accountId).state(BLOCKED).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(IllegalStateException.class, () -> service.confirm(transaction.getId()));
        }

        @Test
        void throws_exception_when_transaction_state_is_not_pending() {
            var transaction = createTransaction(dummyWithdraw().state(REJECTED));
            var accountId = transaction.getAccountId();
            var account = dummyAccount().id(accountId).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(IllegalStateException.class, () -> service.confirm(transaction.getId()));
        }

        @Test
        void updates_account_balance_in_incoming_transaction() {
            var groupId = randomTransactionGroupId();
            var transaction1 = createTransaction(dummyTransfer().groupId(groupId));
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = createTransaction(dummyTransfer().direction(INCOMING).groupId(groupId));
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();

            when(accountService.getById(accountId1))
                    .thenReturn(account1);
            when(accountService.getById(accountId2))
                    .thenReturn(account2);

            service.confirm(transaction1.getId());

            verify(balanceService, times(1))
                    .deposit(accountId2, transaction2.getAmount());
        }

        @Test
        void does_not_update_account_balance_in_outgoing_transfer() {
            var groupId = randomTransactionGroupId();
            var transaction1 = createTransaction(dummyTransfer().groupId(groupId));
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = createTransaction(dummyTransfer().direction(INCOMING).groupId(groupId));
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();

            when(accountService.getById(accountId1))
                    .thenReturn(account1);
            when(accountService.getById(accountId2))
                    .thenReturn(account2);

            service.confirm(transaction1.getId());

            verify(balanceService, times(0))
                    .withdraw(accountId1, transaction1.getAmount());
        }

        @Test
        void updates_account_balance_in_outgoing_transaction() {
            var transaction = createTransaction(dummyWithdraw());
            var accountId = transaction.getAccountId();
            var account = dummyAccount().id(accountId).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);

            service.confirm(transaction.getId());

            verify(balanceService, times(1))
                    .withdraw(accountId, transaction.getAmount());
        }

        @Test
        void updates_transaction_state_in_incoming_transaction() {
            var groupId = randomTransactionGroupId();
            var transaction1 = createTransaction(dummyTransfer().groupId(groupId));
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = createTransaction(dummyTransfer().direction(INCOMING).groupId(groupId));
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();

            when(accountService.getById(accountId1))
                    .thenReturn(account1);
            when(accountService.getById(accountId2))
                    .thenReturn(account2);

            service.confirm(transaction1.getId());

            assertThat(repository.findTransactionById(transaction2.getId()))
                    .get()
                    .returns(transaction2.getAccountId(), Transaction::getAccountId)
                    .returns(transaction2.getUserId(), Transaction::getUserId)
                    .returns(transaction2.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(CONFIRMED, Transaction::getState)
                    .returns(TRANSFER, Transaction::getType)
                    .returns(INCOMING, Transaction::getDirection)
                    .returns(transaction2.getConcept(), Transaction::getConcept);
        }

        @Test
        void updates_transaction_state_in_outgoing_transaction() {
            var groupId = randomTransactionGroupId();
            var transaction1 = createTransaction(dummyTransfer().groupId(groupId));
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = createTransaction(dummyTransfer().direction(INCOMING).groupId(groupId));
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();

            when(accountService.getById(accountId1))
                    .thenReturn(account1);
            when(accountService.getById(accountId2))
                    .thenReturn(account2);

            service.confirm(transaction1.getId());

            assertThat(repository.findTransactionById(transaction1.getId()))
                    .get()
                    .returns(transaction1.getAccountId(), Transaction::getAccountId)
                    .returns(transaction1.getUserId(), Transaction::getUserId)
                    .returns(transaction1.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(CONFIRMED, Transaction::getState)
                    .returns(TRANSFER, Transaction::getType)
                    .returns(OUTGOING, Transaction::getDirection)
                    .returns(transaction1.getConcept(), Transaction::getConcept);
        }
    }

    @Nested
    class Reject {

        @Test
        void throws_not_found_exception_when_transaction_id_does_not_exist() {
            var transactionId = randomTransactionId();

            assertThrows(NotFoundException.class, () -> service.reject(transactionId));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var transaction = createTransaction(dummyWithdraw());
            var accountId = transaction.getAccountId();
            var account = dummyAccount().id(accountId).state(BLOCKED).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(IllegalStateException.class, () -> service.reject(transaction.getId()));
        }

        @Test
        void throws_exception_when_transaction_state_is_not_pending() {
            var transaction = createTransaction(dummyWithdraw().state(CONFIRMED));
            var accountId = transaction.getAccountId();
            var account = dummyAccount().id(accountId).build();

            when(accountService.getById(accountId))
                    .thenReturn(account);

            assertThrows(IllegalStateException.class, () -> service.reject(transaction.getId()));
        }

        @Test
        void updates_account_balance_in_outgoing_transfer() {
            var groupId = randomTransactionGroupId();
            var transaction1 = createTransaction(dummyTransfer().groupId(groupId));
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = createTransaction(dummyTransfer().direction(INCOMING).groupId(groupId));
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();

            when(accountService.getById(accountId1))
                    .thenReturn(account1);
            when(accountService.getById(accountId2))
                    .thenReturn(account2);

            service.reject(transaction1.getId());

            verify(balanceService, times(1))
                    .deposit(accountId1, transaction1.getAmount());
        }

        @Test
        void updates_transaction_to_rejected() {
            var groupId = randomTransactionGroupId();
            var transaction1 = createTransaction(dummyTransfer().groupId(groupId));
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = createTransaction(dummyTransfer().direction(INCOMING).groupId(groupId));
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();

            when(accountService.getById(accountId1))
                    .thenReturn(account1);
            when(accountService.getById(accountId2))
                    .thenReturn(account2);

            service.reject(transaction1.getId());

            assertThat(repository.findTransactionById(transaction1.getId()))
                    .get()
                    .returns(REJECTED, Transaction::getState);
            assertThat(repository.findTransactionById(transaction2.getId()))
                    .get()
                    .returns(REJECTED, Transaction::getState);
        }
    }

    private static AccountAccess access(UserId userId, AccountId accountId) {
        return access(userId, accountId, GRANTED, OWNER);
    }

    private static AccountAccess access(UserId userId, AccountId accountId, AccessState state, AccessType type) {
        return dummyAccountAccess()
                .userId(userId)
                .accountId(accountId)
                .state(state)
                .type(type)
                .build();
    }

    private Transaction createTransaction(Transaction.TransactionBuilder builder) {
        var transaction = builder.build();
        repository.insert(transaction);
        return transaction;
    }
}