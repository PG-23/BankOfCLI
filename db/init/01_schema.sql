-- Custom enum type for transaction type
CREATE TYPE transaction_type AS ENUM ('deposit', 'withdrawal', 'transfer');

-- Accounts table
CREATE TABLE accounts (
        id SERIAL PRIMARY KEY,
        pin CHAR(4) NOT NULL,
        balance NUMERIC(12, 2) NOT NULL DEFAULT 0.00
);

-- Transactions table
CREATE TABLE transactions (
        id SERIAL PRIMARY KEY,
        type transaction_type NOT NULL,
        amount NUMERIC(12, 2) NOT NULL,
        from_account_id INTEGER REFERENCES accounts(id),
        to_account_id INTEGER REFERENCES accounts(id),
        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT chk_at_least_one_account
            CHECK (from_account_id IS NOT NULL OR to_account_id IS NOT NULL)
);