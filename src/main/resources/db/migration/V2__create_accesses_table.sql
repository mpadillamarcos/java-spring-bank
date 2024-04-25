CREATE TABLE accesses (
    account_id uuid not null,
    user_id uuid not null,
    created_date timestamp not null,
    type text not null,
    state text not null
)