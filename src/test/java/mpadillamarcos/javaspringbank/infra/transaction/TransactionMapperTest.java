package mpadillamarcos.javaspringbank.infra.transaction;

import mpadillamarcos.javaspringbank.infra.MapperTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static mpadillamarcos.javaspringbank.domain.Instances.*;
import static mpadillamarcos.javaspringbank.domain.account.AccountId.randomAccountId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionDirection.INCOMING;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionGroupId.randomTransactionGroupId;
import static mpadillamarcos.javaspringbank.domain.transaction.TransactionId.randomTransactionId;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TransactionMapper.class})
public class TransactionMapperTest extends MapperTestBase {

    @Autowired
    private TransactionMapper mapper;

    @Test
    void returns_nothing_when_there_are_no_transactions() {
        var transaction = mapper.findTransactionById(randomTransactionId());

        assertThat(transaction).isEmpty();
    }

    @Test
    void returns_inserted_transaction() {
        var transaction = dummyWithdraw().build();
        mapper.insert(transaction);

        var storedTransaction = mapper.findTransactionById(transaction.getId());

        assertThat(storedTransaction).hasValue(transaction);
    }

    @Test
    void updates_transaction_state() {
        var transaction = dummyDeposit().build();
        mapper.insert(transaction);
        var updatedTransaction = transaction.confirm();
        mapper.update(updatedTransaction);

        var storedTransaction = mapper.findTransactionById(transaction.getId());

        assertThat(storedTransaction).hasValue(updatedTransaction);
    }

    @Test
    void returns_a_list_of_transactions_given_a_group_id() {
        var groupId = randomTransactionGroupId();
        var transaction1 = dummyTransfer().groupId(groupId).build();
        var transaction2 = dummyTransfer().groupId(groupId).direction(INCOMING).build();
        var transaction3 = dummyWithdraw().build();
        mapper.insert(transaction1);
        mapper.insert(transaction2);
        mapper.insert(transaction3);

        var storedTransactions = mapper.findTransactionsByGroupId(groupId);

        assertThat(storedTransactions).containsExactly(transaction1, transaction2);
    }

    @Test
    void returns_a_list_of_transactions_given_an_account_id() {
        var accountId = randomAccountId();
        var transaction1 = dummyTransfer().accountId(accountId).build();
        var transaction2 = dummyTransfer().direction(INCOMING).build();
        var transaction3 = dummyWithdraw().accountId(accountId).build();
        mapper.insert(transaction1);
        mapper.insert(transaction2);
        mapper.insert(transaction3);

        var storedTransactions = mapper.findTransactionsByAccountId(accountId);

        assertThat(storedTransactions).containsExactly(transaction1, transaction3);
    }
}
