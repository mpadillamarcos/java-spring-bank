package mpadillamarcos.javaspringbank.domain.transaction;

import mpadillamarcos.javaspringbank.domain.access.AccountAccessService;
import mpadillamarcos.javaspringbank.domain.account.AccountService;
import mpadillamarcos.javaspringbank.domain.balance.BalanceService;
import mpadillamarcos.javaspringbank.domain.exception.InsufficientBalanceException;
import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import mpadillamarcos.javaspringbank.infra.TestClock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

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
}
