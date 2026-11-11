-- HINT: Create expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- HINT: Create indexes for frequently queried columns
CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_expenses_deleted ON expenses(is_deleted);