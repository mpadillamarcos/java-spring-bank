CREATE TABLE transactions (
    id uuid not null,
    group_id uuid not null,
    user_id uuid not null,
    account_id uuid not null,
    amount numeric not null,
    currency text not null,
    created_date timestamp not null,
    state text not null,
    direction text not null,
    type text not null,
    concept text
)