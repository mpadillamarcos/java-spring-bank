CREATE TABLE balances (
    account_id uuid not null,
    amount numeric not null,
    currency text not null
)