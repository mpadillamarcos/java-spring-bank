package mpadillamarcos.javaspringbank;

import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.infra.DbTestBase;
import mpadillamarcos.javaspringbank.web.account.AccountViewDto;
import mpadillamarcos.javaspringbank.web.transaction.TransactionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.UUID.randomUUID;
import static mpadillamarcos.javaspringbank.domain.money.Currency.EUR;
import static mpadillamarcos.javaspringbank.domain.money.Money.eur;
import static mpadillamarcos.javaspringbank.domain.money.Money.zero;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.OUTGOING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.CONFIRMED;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionState.PENDING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class JavaSpringBankApplicationTests extends DbTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void can_deposit_money() {
        var userId = randomUUID();
        var account = openAccount(userId);
        assertThat(account.getBalance()).isEqualTo(zero(EUR));

        deposit(account, eur(170));
        assertThatCurrentBalanceIs(account, eur(170));

        var transactions = listTransactions(account.getId());
        assertThat(transactions)
                .hasSize(1)
                .first()
                .returns(CONFIRMED, TransactionDto::getState)
                .returns(DEPOSIT, TransactionDto::getType);
    }

    @Test
    void can_withdraw_money() {
        var userId = randomUUID();
        var account = openAccount(userId);
        deposit(account, eur(170));

        withdraw(account, eur(20));
        assertThatCurrentBalanceIs(account, eur(150));

        var transactions = listTransactions(account.getId());
        assertThat(transactions)
                .hasSize(2)
                .first()
                .returns(CONFIRMED, TransactionDto::getState)
                .returns(WITHDRAW, TransactionDto::getType);
    }

    @Test
    void can_transfer_money() {
        var sender = randomUUID();
        var senderAccount = openAccount(sender);
        deposit(senderAccount, eur(170));

        var beneficiary = randomUUID();
        var beneficiaryAccount = openAccount(beneficiary);

        transfer(senderAccount, beneficiaryAccount, eur(20));
        assertThatCurrentBalanceIs(senderAccount, eur(150));

        var senderTransaction = listTransactions(senderAccount.getId());
        assertThat(senderTransaction)
                .hasSize(2)
                .first()
                .returns(PENDING, TransactionDto::getState)
                .returns(OUTGOING, TransactionDto::getDirection)
                .returns("transfer test", TransactionDto::getConcept)
                .returns(TRANSFER, TransactionDto::getType);
        var beneficiaryTransaction = listTransactions(beneficiaryAccount.getId());
        assertThat(beneficiaryTransaction)
                .hasSize(1)
                .first()
                .returns(PENDING, TransactionDto::getState)
                .returns(INCOMING, TransactionDto::getDirection)
                .returns("transfer test", TransactionDto::getConcept)
                .returns(TRANSFER, TransactionDto::getType);

        confirmLastTransaction(senderAccount);

        assertThatCurrentBalanceIs(beneficiaryAccount, eur(20));
        assertThatCurrentBalanceIs(senderAccount, eur(150));
    }

    @Test
    void can_reject_transaction() {
        var sender = randomUUID();
        var senderAccount = openAccount(sender);
        deposit(senderAccount, eur(170));

        var beneficiary = randomUUID();
        var beneficiaryAccount = openAccount(beneficiary);

        transfer(senderAccount, beneficiaryAccount, eur(20));
        assertThatCurrentBalanceIs(senderAccount, eur(150));

        rejectLastTransaction(senderAccount);
        assertThatCurrentBalanceIs(senderAccount, eur(170));
        assertThatCurrentBalanceIs(beneficiaryAccount, zero(EUR));
    }

    private void assertThatCurrentBalanceIs(AccountViewDto account, Money expectedBalance) {
        var updatedAccount = getAccount(account.getUserId(), account.getId());

        assertThat(updatedAccount.getBalance()).isEqualTo(expectedBalance);
    }

    private List<TransactionDto> listTransactions(UUID accountId) {
        return asList(restTemplate.getForObject("/accounts/" + accountId + "/transactions", TransactionDto[].class));
    }

    private AccountViewDto getAccount(UUID userId, UUID accountId) {
        return restTemplate.getForObject("/users/" + userId + "/accounts/" + accountId, AccountViewDto.class);
    }

    private AccountViewDto openAccount(UUID userId) {
        return restTemplate.postForObject("/users/" + userId + "/accounts", emptyMap(), AccountViewDto.class);
    }

    private void deposit(AccountViewDto account, Money amount) {
        var url = "/users/" + account.getUserId() + "/accounts/" + account.getId() + "/deposit";

        restTemplate.postForObject(url, Map.of("amount", amount), Void.class);
    }

    private void withdraw(AccountViewDto account, Money amount) {
        var url = "/users/" + account.getUserId() + "/accounts/" + account.getId() + "/withdraw";

        restTemplate.postForObject(url, Map.of("amount", amount), Void.class);
    }

    private void transfer(AccountViewDto originAccount, AccountViewDto destinationAccount, Money amount) {
        var url = "/users/" + originAccount.getUserId() + "/accounts/" + originAccount.getId() + "/transfer";

        restTemplate.postForObject(url, Map.of(
                "amount", amount,
                "destinationAccountId", destinationAccount.getId(),
                "concept", "transfer test"
        ), Void.class);
    }

    private void confirmLastTransaction(AccountViewDto account) {
        var lastTransaction = listTransactions(account.getId()).get(0);
        var url = "/transactions/" + lastTransaction.getId() + "/confirm";

        restTemplate.postForObject(url, emptyMap(), Void.class);
    }

    private void rejectLastTransaction(AccountViewDto account) {
        var lastTransaction = listTransactions(account.getId()).get(0);
        var url = "/transactions/" + lastTransaction.getId() + "/reject";

        restTemplate.postForObject(url, emptyMap(), Void.class);
    }
}
