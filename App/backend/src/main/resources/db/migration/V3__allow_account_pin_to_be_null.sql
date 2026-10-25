-- Allow accounts to be created before the user sets their PIN.
-- PIN verification remains enforced by the application for transactions.
ALTER TABLE accounts
MODIFY COLUMN pin VARCHAR(255) NULL;
