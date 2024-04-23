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

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static mpadillamarcos.javaspringbank.domain.Instances.*;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.GRANTED;
import static mpadillamarcos.javaspringbank.domain.access.AccessState.REVOKED;
import static mpadillamarcos.javaspringbank.domain.access.AccessType.*;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.BLOCKED;
import static mpadillamarcos.javaspringbank.domain.account.AccountState.CLOSED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.randomTransactionId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.CONFIRMED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.DECLINED;
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
            var originAccountId = request.getAccountId();

            when(accountService.findById(originAccountId))
                    .thenReturn(empty());

            assertThrows(NotFoundException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_origin_account_state_is_not_open() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).state(BLOCKED).build();

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));

            assertThrows(TransactionNotAllowedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_user_has_no_access_to_origin_account() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(empty());

            assertThrows(AccessDeniedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_user_access_is_revoked() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(request, REVOKED, OPERATOR);

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_user_type_is_viewer() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(request, GRANTED, VIEWER);

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.transfer(request));
        }

        @Test
        void throws_not_found_exception_when_destination_account_does_not_exist() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(request);

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.findById(destinationAccountId))
                    .thenReturn(empty());

            assertThrows(NotFoundException.class, () -> service.transfer(request));
        }

        @Test
        void throws_exception_when_destination_account_state_is_not_open() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(request);
            var destinationAccount = dummyAccount().id(destinationAccountId).state(CLOSED).build();

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.findById(destinationAccountId))
                    .thenReturn(Optional.of(destinationAccount));

            assertThrows(TransactionNotAllowedException.class, () -> service.transfer(request));
        }

        @Test
        void updates_origin_account_balance() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(request);
            var destinationAccount = dummyAccount().id(destinationAccountId).build();

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.findById(destinationAccountId))
                    .thenReturn(Optional.of(destinationAccount));

            service.transfer(request);

            verify(balanceService, times(1))
                    .withdraw(originAccountId, request.getAmount());
        }

        @Test
        void creates_new_transaction_for_the_origin_account_user() {
            var request = dummyTransferRequest();
            var userId = request.getUserId();
            var originAccountId = request.getAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(request);
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransfer()
                    .accountId(originAccountId)
                    .userId(userId)
                    .type(TRANSFER)
                    .build();

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.findById(destinationAccountId))
                    .thenReturn(Optional.of(destinationAccount));

            service.transfer(request);

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
            var originAccountId = request.getAccountId();
            var destinationAccountId = request.getDestinationAccountId();
            var originAccount = dummyAccount().userId(userId).id(originAccountId).build();
            var access = access(request);
            var destinationAccount = dummyAccount().id(destinationAccountId).build();
            var newTransaction = dummyTransfer()
                    .accountId(destinationAccountId)
                    .userId(userId)
                    .type(TRANSFER)
                    .direction(INCOMING)
                    .build();

            when(accountService.findById(originAccountId))
                    .thenReturn(Optional.of(originAccount));
            when(accessService.findAccountAccess(originAccountId, userId))
                    .thenReturn(Optional.of(access));
            when(accountService.findById(destinationAccountId))
                    .thenReturn(Optional.of(destinationAccount));

            service.transfer(request);

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
        void throws_not_found_when_account_does_not_exist() {
            var request = dummyWithdrawRequest();
            var accountId = request.getAccountId();

            when(accountService.findById(accountId))
                    .thenReturn(empty());

            assertThrows(NotFoundException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));

            assertThrows(TransactionNotAllowedException.class, () -> service.withdraw(request));
        }

        @Test
        void throws_exception_when_user_has_no_access_to_account() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
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
            var access = access(request, REVOKED, OPERATOR);

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
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
            var access = access(request, GRANTED, VIEWER);

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
            when(accessService.findAccountAccess(request.getAccountId(), request.getUserId()))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.withdraw(request));
        }

        @Test
        void updates_account_balance() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(request);

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            service.withdraw(request);

            verify(balanceService, times(1))
                    .withdraw(accountId, request.getAmount());
        }

        @Test
        void creates_new_withdrawal() {
            var request = dummyWithdrawRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(request);
            var newTransaction = dummyWithdraw()
                    .accountId(accountId)
                    .userId(userId)
                    .type(WITHDRAW)
                    .build();

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

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
    }

    @Nested
    class Deposit {

        @Test
        void throws_not_found_when_account_does_not_exist() {
            var request = dummyDepositRequest();
            var accountId = request.getAccountId();

            when(accountService.findById(accountId))
                    .thenReturn(empty());

            assertThrows(NotFoundException.class, () -> service.deposit(request));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).state(BLOCKED).build();

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));

            assertThrows(TransactionNotAllowedException.class, () -> service.deposit(request));
        }

        @Test
        void throws_exception_when_user_has_no_access_to_account() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
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
            var access = access(request, REVOKED, OPERATOR);

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
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
            var access = access(request, GRANTED, VIEWER);

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            assertThrows(AccessDeniedException.class, () -> service.deposit(request));
        }

        @Test
        void updates_account_balance() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(request);

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            service.deposit(request);

            verify(balanceService, times(1))
                    .deposit(accountId, request.getAmount());
        }

        @Test
        void creates_new_deposit() {
            var request = dummyDepositRequest();
            var userId = request.getUserId();
            var accountId = request.getAccountId();
            var account = dummyAccount().userId(userId).id(accountId).build();
            var access = access(request);
            var newTransaction = dummyDeposit()
                    .accountId(accountId)
                    .userId(userId)
                    .type(DEPOSIT)
                    .build();

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));
            when(accessService.findAccountAccess(accountId, userId))
                    .thenReturn(Optional.of(access));

            service.deposit(request);

            assertThat(repository.findLastTransactionByAccountId(accountId))
                    .get()
                    .returns(accountId, Transaction::getAccountId)
                    .returns(userId, Transaction::getUserId)
                    .returns(newTransaction.getAmount(), Transaction::getAmount)
                    .returns(NOW, Transaction::getCreatedDate)
                    .returns(newTransaction.getState(), Transaction::getState)
                    .returns(DEPOSIT, Transaction::getType)
                    .returns(INCOMING, Transaction::getDirection)
                    .returns(newTransaction.getConcept(), Transaction::getConcept);
        }
    }

    @Nested
    class ConfirmTransaction {

        @Test
        void throws_not_found_exception_when_transaction_id_does_not_exist() {
            var transactionId = randomTransactionId();

            assertThrows(NotFoundException.class, () -> service.confirmTransaction(transactionId));
        }

        @Test
        void gets_multiple_transactions_by_group_id() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var transaction2 = dummyTransfer().direction(INCOMING).groupId(groupId).build();
            repository.insert(transaction1);
            repository.insert(transaction2);

            assertThat(repository.findTransactionsByGroupId(groupId))
                    .isEqualTo(List.of(transaction1, transaction2));
        }

        @Test
        void throws_exception_when_transaction_state_is_not_pending() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = dummyTransfer().direction(INCOMING).state(DECLINED).groupId(groupId).build();
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();
            repository.insert(transaction1);
            repository.insert(transaction2);

            when(accountService.findById(accountId1))
                    .thenReturn(Optional.of(account1));
            when(accountService.findById(accountId2))
                    .thenReturn(Optional.of(account2));

            assertThrows(IllegalStateException.class, () -> service.confirmTransaction(transaction1.getId()));
        }

        @Test
        void throws_exception_when_account_does_not_exist() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = dummyTransfer().direction(INCOMING).groupId(groupId).build();
            var accountId2 = transaction2.getAccountId();
            repository.insert(transaction1);
            repository.insert(transaction2);

            when(accountService.findById(accountId1))
                    .thenReturn(Optional.of(account1));
            when(accountService.findById(accountId2))
                    .thenReturn(empty());

            assertThrows(NotFoundException.class, () -> service.confirmTransaction(transaction1.getId()));
        }

        @Test
        void throws_exception_when_account_state_is_not_open() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = dummyTransfer().direction(INCOMING).groupId(groupId).build();
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).state(CLOSED).build();
            repository.insert(transaction1);
            repository.insert(transaction2);

            when(accountService.findById(accountId1))
                    .thenReturn(Optional.of(account1));
            when(accountService.findById(accountId2))
                    .thenReturn(Optional.of(account2));

            assertThrows(IllegalStateException.class, () -> service.confirmTransaction(transaction1.getId()));
        }

        @Test
        void updates_account_balance_in_incoming_transaction() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = dummyTransfer().direction(INCOMING).groupId(groupId).build();
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();
            repository.insert(transaction1);
            repository.insert(transaction2);

            when(accountService.findById(accountId1))
                    .thenReturn(Optional.of(account1));
            when(accountService.findById(accountId2))
                    .thenReturn(Optional.of(account2));

            service.confirmTransaction(transaction1.getId());

            verify(balanceService, times(1))
                    .deposit(accountId2, transaction2.getAmount());
        }

        @Test
        void does_not_update_account_balance_in_outgoing_transfer() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = dummyTransfer().direction(INCOMING).groupId(groupId).build();
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();
            repository.insert(transaction1);
            repository.insert(transaction2);

            when(accountService.findById(accountId1))
                    .thenReturn(Optional.of(account1));
            when(accountService.findById(accountId2))
                    .thenReturn(Optional.of(account2));

            service.confirmTransaction(transaction1.getId());

            verify(balanceService, times(0))
                    .withdraw(accountId1, transaction1.getAmount());
        }

        @Test
        void updates_account_balance_in_outgoing_transaction() {
            var transaction = dummyWithdraw().build();
            var accountId = transaction.getAccountId();
            var account = dummyAccount().id(accountId).build();
            repository.insert(transaction);

            when(accountService.findById(accountId))
                    .thenReturn(Optional.of(account));

            service.confirmTransaction(transaction.getId());

            verify(balanceService, times(1))
                    .withdraw(accountId, transaction.getAmount());
        }

        @Test
        void updates_transaction_state_in_incoming_transfer() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = dummyTransfer().direction(INCOMING).groupId(groupId).build();
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();
            repository.insert(transaction1);
            repository.insert(transaction2);

            when(accountService.findById(accountId1))
                    .thenReturn(Optional.of(account1));
            when(accountService.findById(accountId2))
                    .thenReturn(Optional.of(account2));

            service.confirmTransaction(transaction1.getId());

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
        void updates_transaction_state_in_outgoing_transfer() {
            var groupId = randomTransactionGroupId();
            var transaction1 = dummyTransfer().groupId(groupId).build();
            var accountId1 = transaction1.getAccountId();
            var account1 = dummyAccount().id(accountId1).build();
            var transaction2 = dummyTransfer().direction(INCOMING).groupId(groupId).build();
            var accountId2 = transaction2.getAccountId();
            var account2 = dummyAccount().id(accountId2).build();
            repository.insert(transaction1);
            repository.insert(transaction2);

            when(accountService.findById(accountId1))
                    .thenReturn(Optional.of(account1));
            when(accountService.findById(accountId2))
                    .thenReturn(Optional.of(account2));

            service.confirmTransaction(transaction1.getId());

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


    private static AccountAccess access(OperationRequest request) {
        return access(request, GRANTED, OWNER);
    }

    private static AccountAccess access(OperationRequest request, AccessState state, AccessType type) {
        return dummyAccountAccess()
                .userId(request.getUserId())
                .accountId(request.getAccountId())
                .state(state)
                .type(type)
                .build();
    }
}