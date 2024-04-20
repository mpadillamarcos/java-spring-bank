CREATE TABLE accounts (
    id uuid not null,
    user_id uuid not null,
    created_date timestamp not null,
    state text not null
)