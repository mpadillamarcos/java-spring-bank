package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.SneakyThrows;
import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.account.AccountView;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.InsufficientBalanceException;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import mpadillamarcos.javaspringbank.infra.TestClock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static java.util.stream.IntStream.range;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyTransfer;
import static mpadillamarcos.javaspringbank.domain.Instances.dummyWithdraw;
import static mpadillamarcos.javaspringbank.domain.money.Money.eur;
import static mpadillamarcos.javaspringbank.domain.transaction.DepositRequest.depositRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.PENDING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransferRequest.transferRequest;
import static mpadillamarcos.javaspringbank.domain.transaction.WithdrawRequest.withdrawRequest;
import static mpadillamarcos.javaspringbank.domain.user.UserId.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(
        classes = {
                TransactionService.class,
                AccountService.class,
                AccountAccessService.class,
                BalanceService.class,
                TestClock.class
        }
)
public class TransactionServiceIT extends MapperTestBase {

    @SpyBean
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;
    @SpyBean
    private BalanceService balanceService;
    @Autowired
    private AccountService accountService;

    @Nested
    class Transfer {

        @Test
        void rolls_changes_back_when_transaction_is_not_inserted() {
            var sender = randomUserId();
            var beneficiary = randomUserId();
            var initialBalance = eur(200);
            var senderAccount = accountService.openAccount(sender);
            var beneficiaryAccount = accountService.openAccount(beneficiary);
            balanceService.deposit(senderAccount.getAccountId(), initialBalance);
            doThrow(RuntimeException.class).when(transactionRepository).insert(any());

            assertThrows(RuntimeException.class, () -> transactionService.transfer(
                    transferRequest()
                            .userId(sender)
                            .originAccountId(senderAccount.getAccountId())
                            .destinationAccountId(beneficiaryAccount.getAccountId())
                            .amount(eur(50))
                            .build()
            ));

            assertThat(balanceService.getBalance(senderAccount.getAccountId()).getAmount())
                    .isEqualTo(initialBalance);
        }
    }

    @Nested
    class Withdraw {

        @Test
        void rolls_changes_back_when_withdrawal_transaction_is_updated() {
            var userId = randomUserId();
            var account = accountService.openAccount(userId);
            var initialBalance = eur(200);
            balanceService.deposit(account.getAccountId(), initialBalance);
            doThrow(RuntimeException.class).when(transactionRepository).update(any());

            assertThrows(RuntimeException.class, () -> transactionService.withdraw(
                    withdrawRequest()
                            .accountId(account.getAccountId())
                            .userId(userId)
                            .amount(eur(150))
                            .build()
            ));

            assertThat(transactionRepository.findTransactionsByAccountId(account.getAccountId())).isEmpty();
        }
    }

    @Nested
    class Deposit {

        @Test
        void rolls_changes_back_when_deposit_transaction_is_not_updated() {
            var userId = randomUserId();
            var account = accountService.openAccount(userId);
            doThrow(RuntimeException.class).when(transactionRepository).update(any());

            assertThrows(RuntimeException.class, () -> transactionService.deposit(
                    depositRequest()
                            .accountId(account.getAccountId())
                            .userId(userId)
                            .amount(eur(150))
                            .build()
            ));

            assertThat(transactionRepository.findTransactionsByAccountId(account.getAccountId())).isEmpty();
        }
    }

    @Nested
    class Confirm {

        @Test
        void rolls_changes_back_when_is_not_possible_to_confirm() {
            var userId = randomUserId();
            var account = accountService.openAccount(userId);
            var transaction = dummyWithdraw().accountId(account.getAccountId()).userId(userId).build();
            transactionRepository.insert(transaction);

            doThrow(InsufficientBalanceException.class).when(balanceService).withdraw(any(), any());

            assertThrows(InsufficientBalanceException.class, () -> transactionService.confirm(transaction.getId()));

            assertThat(transactionRepository.findTransactionById(transaction.getId())).get()
                    .returns(PENDING, Transaction::getState);
        }
    }

    @Nested
    class Reject {

        @Test
        void rolls_changes_back_when_is_not_possible_to_reject() {
            var userId = randomUserId();
            var account = accountService.openAccount(userId);
            var transaction = dummyTransfer().accountId(account.getAccountId()).userId(userId).build();
            transactionRepository.insert(transaction);

            doThrow(RuntimeException.class).when(balanceService).deposit(any(), any());

            assertThrows(RuntimeException.class, () -> transactionService.reject(transaction.getId()));

            assertThat(transactionRepository.findTransactionById(transaction.getId())).get()
                    .returns(PENDING, Transaction::getState);
        }
    }

    @Nested
    class Concurrency {

        @Test
        void updates_balances_concurrently() {
            var account1 = setupAccount(eur(2_000));
            var account2 = setupAccount(eur(2_000));

            var task1 = new TransferTask(account2, account1, eur(100));
            var task2 = new TransferTask(account1, account2, eur(100));

            runTimes(20, task1, task2);

            assertThatBalanceIs(account1, eur(2_000));
            assertThatBalanceIs(account2, eur(2_000));
        }

        @Test
        void confirms_transaction_concurrently() {
            var originAccount = setupAccount(eur(2000));
            var destinationAccount = setupAccount(eur(0));
            var originAccountId = originAccount.getAccountId();
            var destinationAccountId = destinationAccount.getAccountId();

            var transactionId = transactionService.transfer(transferRequest()
                    .destinationAccountId(destinationAccountId)
                    .originAccountId(originAccountId)
                    .userId(originAccount.getUserId())
                    .amount(eur(10))
                    .build());

            var task = new ConfirmTask(transactionId);

            runTimes(task, 20);

            assertThatBalanceIs(destinationAccount, eur(10));
        }

        @Test
        void rejects_transaction_concurrently() {
            var originAccount = setupAccount(eur(2000));
            var destinationAccount = setupAccount(eur(100));
            var originAccountId = originAccount.getAccountId();
            var destinationAccountId = destinationAccount.getAccountId();

            var transactionId = transactionService.transfer(transferRequest()
                    .destinationAccountId(destinationAccountId)
                    .originAccountId(originAccountId)
                    .userId(originAccount.getUserId())
                    .amount(eur(10))
                    .build());

            var task = new RejectTask(transactionId);

            runTimes(task, 20);

            assertThatBalanceIs(originAccount, eur(2000));
        }

        private void assertThatBalanceIs(AccountView account, Money expected) {
            var balance = balanceService.getBalance(account.getAccountId()).getAmount();

            assertThat(balance).isEqualTo(expected);
        }

        private AccountView setupAccount(Money initialBalance) {
            var user = randomUserId();
            var account = accountService.openAccount(user);

            transactionService.deposit(
                    depositRequest()
                            .accountId(account.getAccountId())
                            .amount(initialBalance)
                            .userId(user)
                            .build()
            );
            return account;
        }

        private void runTimes(int times, Runnable... tasks) {
            try (var executor = Executors.newFixedThreadPool(20)) {
                var repeatedTasks = range(0, times)
                        .mapToObj(i -> tasks)
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList());
                Collections.shuffle(repeatedTasks);

                var futures = repeatedTasks.stream()
                        .map(executor::submit)
                        .toList();

                await(futures);
            }
        }

        private void runTimes(Runnable task, int times) {
            try (var executor = Executors.newFixedThreadPool(20)) {
                var futures = range(0, times)
                        .mapToObj(i -> executor.submit(task))
                        .toList();
                await(futures);
            }
        }

        @SneakyThrows
        private void await(List<? extends Future<?>> futures) {
            while (!futures.stream().allMatch(Future::isDone)) {
                sleep(200);
            }
        }

        private class TransferTask implements Runnable {

            private final AccountView destinationAccount;
            private final AccountView originAccount;
            private final Money amount;

            private TransferTask(AccountView destinationAccount, AccountView originAccount, Money amount) {
                this.destinationAccount = destinationAccount;
                this.originAccount = originAccount;
                this.amount = amount;
            }

            @Override
            public void run() {
                var originAccountId = originAccount.getAccountId();
                var destinationAccountId = destinationAccount.getAccountId();

                var transactionId = transactionService.transfer(transferRequest()
                        .destinationAccountId(destinationAccountId)
                        .originAccountId(originAccountId)
                        .userId(originAccount.getUserId())
                        .amount(amount)
                        .build());

                transactionService.confirm(transactionId);
            }
        }

        private class ConfirmTask implements Runnable {

            private final TransactionId id;

            private ConfirmTask(TransactionId id) {
                this.id = id;
            }

            @Override
            public void run() {
                transactionService.confirm(id);
            }
        }

        private class RejectTask implements Runnable {

            private final TransactionId id;

            public RejectTask(TransactionId id) {
                this.id = id;
            }

            @Override
            public void run() {
                transactionService.reject(id);
            }
        }
    }
}
