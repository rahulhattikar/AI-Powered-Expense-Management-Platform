# Expense Tracker Database Design

## User

- id (PK)
- name
- email
- password
- created_at
- updated_at

## Expense

- id (PK)
- user_id (FK)
- amount
- description
- category
- expense_date
- created_at
- updated_at

## Budget

- id (PK)
- user_id (FK)
- category
- monthly_limit
- created_at
- updated_at
