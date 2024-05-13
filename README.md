# java-spring-bank :bank:

## Description

This project is a bank REST API that offers an illustration of banking functionalities,
from creating an account and managing account access to transaction processing. It was created as a learning project to
gain proficiency in both Java and Spring Boot.

## Build the application locally

To manage the project's build I've used **Maven**.

* Set your docker engine running.
* To run the tests, run: `.\mvnw.cmd test`
* To create the jar file, run: `.\mvnw.cmd package`

## Business

The app structure revolves around 4 main classes: account, access, transactions and balance.
One account has only one balance but may have many transactions (transfer, deposit or withdraw)
and also different users can have different types of access to that account.

<img src="basic_diagram.png" width="400" alt="domain schema" >

An example of user flow would be making a transfer between 2 accounts. To create a transfer
both accounts must be open and the user creating the transfer must be the owner of the account or
have operator access.
![transfer flow](transfer_flow.png)

## Tech Stack

The bank app is built using Java and Spring Boot framework.

**Libraries**:

- **Lombok**: to reduce boilerplate code by using annotations.
- **MyBatis**: to interact with PostgreSQL using an XML descriptor to map Java objects to database tables with minimal
  boilerplate code.
- **JUnit**: to write and run tests.
- **Mockito**: to create mock objects in unit tests to isolate the behavior of the class being tested from its
  dependencies.
- **Testcontainers**: to create disposable containers (Docker containers) for the integration tests.
- **Flyway**: to manage the evolution of database schemas through series of versioned SQL migration scripts.

## Testing approach

<details>
<summary>Unit Tests</summary>

To test individual classes and functionalities.

```java

@Test
void sets_state_to_blocked_when_blocking_an_open_account() {
    var account = dummyAccount().build();

    var blocked = account.block();

    assertThat(blocked.getState()).isEqualTo(BLOCKED);
}
```

</details>

<details>
<summary>Integration Tests</summary>

To test the interaction between classes and a volatile database.

```java

@Test
void returns_one_inserted_access() {
    var access = dummyAccountAccess().build();
    mapper.insert(access);

    var storedAccess = mapper.findAccountAccess(access.getAccountId(), access.getUserId());

    assertThat(storedAccess).hasValue(access);
}
```

</details>

<details>
<summary>Concurrency Tests</summary>

To assure that there are no concurrency issues when sending multiple petitions at once.

```java

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
```

</details>

<details>
<summary>End-to-End Tests</summary>

To test the app functionality from creating an account to creating the different types of
transactions.

```java

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
```

</details>

## Challenges

One of the main challenges was dealing with concurrency because any concurrency issue would have serious consequences
in the account balance when transferring money from one account to another. Testing was a challenge from the start as
this
is the first project I've worked with TDD. Furthermore, object-oriented programming was a whole other challenge,
requiring
careful design considerations to create a scalable and easily maintainable code.

