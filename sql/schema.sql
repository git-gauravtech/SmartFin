-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS finance_app_db;

-- Use the created database
USE finance_app_db;

-- Table for Users
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Storing hashed passwords
    password_salt VARCHAR(255) NOT NULL, -- Storing salt used for hashing
    is_admin BOOLEAN DEFAULT FALSE,     -- Flag to identify admin users
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for Accounts
-- Each user can have multiple accounts (e.g., Savings, Checking, Credit Card)
CREATE TABLE IF NOT EXISTS accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    initial_balance DECIMAL(10, 2) DEFAULT 0.00, -- Initial balance of the account
    current_balance DECIMAL(10, 2) DEFAULT 0.00, -- Dynamic current balance (calculated by app logic, or derived from initial + transactions for dummy data)
    account_type VARCHAR(50), -- e.g., 'Checking', 'Savings', 'Credit Card', 'Cash', 'Investment', 'Loan'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE (user_id, account_name) -- A user cannot have two accounts with the same name
);

-- Table for Categories
-- Users can define their own categories
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL, -- User who owns this custom category
    category_name VARCHAR(100) NOT NULL,
    category_type ENUM('Income', 'Expense') NOT NULL, -- Is this an income or expense category?
    is_default BOOLEAN DEFAULT FALSE, -- Flag for system-provided default categories
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE (user_id, category_name) -- A user cannot have two categories with the same name
);

-- Table for Transactions (MODIFIED)
-- Now includes account_id to link transactions to specific accounts
-- The category_id will link to the new categories table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT NOT NULL, -- New: Link to account
    category_id INT NOT NULL, -- New: Link to category
    amount DECIMAL(10, 2) NOT NULL,
    type ENUM('Income', 'Expense') NOT NULL, -- Redundant with category_type, but kept for clarity/compatibility
    description TEXT,
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

-- Table for Budgets (MODIFIED)
-- Now links to category_id instead of category_name string
CREATE TABLE IF NOT EXISTS budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL, -- New: Link to category
    amount_limit DECIMAL(10, 2) NOT NULL,
    month INT NOT NULL, -- Month (1-12)
    year INT NOT NULL,  -- Year (e.g., 2024)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, category_id, month, year), -- Ensures one budget per category per month/year per user
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

--
-- DUMMY DATA INSERTS BELOW
--

-- Insert Dummy Users
-- Passwords: 'ayushmaan1234' for ayushmaan, 'gaurav1234' for gaurav
INSERT INTO users (username, password_hash, password_salt, is_admin)
VALUES ('ayushmaan', 'pU0d1b11H/n/42jB2y0+8rK2tGg4g3i4o9v4s9e4q8w4i7f5g2j2o7p7q8u2w7y4a5f3s7e4d8c6b2n6m4j7g9h1k0l9m4n2b1v2c6x4z7e8d0f1h2j3k4l5m6n7b8v9c0x1z2e3d4f5g6h7j8k9l0m1n2b3v4c5x6z7', 'b0bN9s+cRjZfR2q/Xl+Mqw==', FALSE);

-- Admin user (no personal finance data beyond this user entry)
INSERT INTO users (username, password_hash, password_salt, is_admin)
VALUES ('gaurav', 'oT2p3o4p5q6r7s8t9u0v1w2x3y4z5a6b7c8d9e0f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0z1a2b3c4d5e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z', 'k2kS0l+XcYfM8p+Y2g+PqA==', TRUE);


-- Insert Default Accounts for Ayushmaan (user_id = 1)
-- Account IDs will be 1, 2, 3, 4 respectively
INSERT INTO accounts (user_id, account_name, initial_balance, current_balance, account_type) VALUES
(1, 'Cash', 1000.00, 1000.00, 'Cash'),
(1, 'Main Checking', 15000.00, 15000.00, 'Checking'),
(1, 'Savings', 50000.00, 50000.00, 'Savings'),
(1, 'Credit Card', -5000.00, -5000.00, 'Credit Card'); -- Negative for credit card debt


-- Insert Default Categories for Ayushmaan (user_id = 1)
-- Category IDs will be 1-14 respectively
INSERT IGNORE INTO categories (user_id, category_name, category_type, is_default) VALUES
(1, 'Salary', 'Income', TRUE),
(1, 'Freelance', 'Income', TRUE),
(1, 'Bonus', 'Income', FALSE),
(1, 'Investments Income', 'Income', FALSE),
(1, 'Food & Groceries', 'Expense', TRUE),
(1, 'Transport', 'Expense', TRUE),
(1, 'Utilities', 'Expense', TRUE),
(1, 'Rent', 'Expense', TRUE),
(1, 'Shopping', 'Expense', TRUE),
(1, 'Entertainment', 'Expense', TRUE),
(1, 'Healthcare', 'Expense', TRUE),
(1, 'Education', 'Expense', TRUE),
(1, 'Loan Repayment', 'Expense', FALSE),
(1, 'Miscellaneous', 'Expense', TRUE);


-- Insert Extensive & Realistic Dummy Transactions for Ayushmaan (user_id = 1) (Jan 2024 - June 2025)
-- User ID = 1
-- Account IDs: Cash=1, Main Checking=2, Savings=3, Credit Card=4
-- Category IDs: Salary=1, Food & Groceries=5, Utilities=7, Transport=6, Entertainment=10, Shopping=9, Miscellaneous=14, Investments Income=4, Loan Repayment=13, Bonus=3, Education=12, Healthcare=11

-- Year 2024
-- January 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-01-01'),
(1, 2, 8, 14000.00, 'Expense', 'Jan Rent', '2024-01-03'),
(1, 2, 5, 2500.00, 'Expense', 'Weekly Groceries', '2024-01-05'),
(1, 2, 7, 1800.00, 'Expense', 'Electricity Bill', '2024-01-08'),
(1, 2, 6, 700.00, 'Expense', 'Local Bus Fare', '2024-01-10'),
(1, 2, 10, 600.00, 'Expense', 'Netflix Subscription', '2024-01-15'),
(1, 2, 5, 1200.00, 'Expense', 'Restaurant Dinner', '2024-01-18'),
(1, 4, 9, 3000.00, 'Expense', 'Winter Clothes', '2024-01-22'),
(1, 1, 14, 300.00, 'Expense', 'Misc Expenses', '2024-01-28'),
(1, 3, 4, 5000.00, 'Income', 'Stock Dividends', '2024-01-30');


-- February 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-02-01'),
(1, 2, 5, 2200.00, 'Expense', 'Weekly Groceries', '2024-02-04'),
(1, 2, 6, 850.00, 'Expense', 'Train Tickets', '2024-02-09'),
(1, 1, 5, 1000.00, 'Expense', 'Cafe Lunch', '2024-02-14'),
(1, 2, 11, 1500.00, 'Expense', 'Doctor Visit', '2024-02-19'),
(1, 4, 10, 700.00, 'Expense', 'Concert Tickets', '2024-02-23'),
(1, 2, 13, 2000.00, 'Expense', 'Personal Loan EMI', '2024-02-28');

-- March 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-03-01'),
(1, 2, 8, 14000.00, 'Expense', 'March Rent', '2024-03-03'),
(1, 2, 5, 2600.00, 'Expense', 'Weekly Groceries', '2024-03-07'),
(1, 2, 7, 1500.00, 'Expense', 'Water Bill', '2024-03-12'),
(1, 2, 6, 900.00, 'Expense', 'Cab Fares', '2024-03-16'),
(1, 1, 5, 1500.00, 'Expense', 'Weekend Brunch', '2024-03-20'),
(1, 2, 12, 4000.00, 'Expense', 'Online Course Fee', '2024-03-25');

-- April 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-04-01'),
(1, 2, 5, 2400.00, 'Expense', 'Weekly Groceries', '2024-04-04'),
(1, 2, 6, 750.00, 'Expense', 'Fuel', '2024-04-08'),
(1, 4, 9, 2500.00, 'Expense', 'Books', '2024-04-12'),
(1, 1, 10, 500.00, 'Expense', 'Movie Tickets', '2024-04-17'),
(1, 2, 14, 200.00, 'Expense', 'Small Purchase', '2024-04-25'),
(1, 3, 3, 8000.00, 'Income', 'Performance Bonus', '2024-04-30');

-- May 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-05-01'),
(1, 2, 8, 14000.00, 'Expense', 'May Rent', '2024-05-03'),
(1, 2, 5, 2700.00, 'Expense', 'Weekly Groceries', '2024-05-06'),
(1, 2, 7, 1900.00, 'Expense', 'Internet Bill', '2024-05-09'),
(1, 2, 6, 1000.00, 'Expense', 'Cab to Airport', '2024-05-13'),
(1, 1, 5, 1800.00, 'Expense', 'Dinner with Family', '2024-05-16'),
(1, 2, 11, 800.00, 'Expense', 'Medicine Purchase', '2024-05-20');

-- June 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-06-01'),
(1, 2, 5, 2800.00, 'Expense', 'Weekly Groceries', '2024-06-05'),
(1, 2, 6, 950.00, 'Expense', 'Local Travel', '2024-06-10'),
(1, 4, 9, 3500.00, 'Expense', 'New Gadget', '2024-06-15'),
(1, 1, 5, 1600.00, 'Expense', 'Dining out', '2024-06-18');


-- July 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-07-01'),
(1, 2, 8, 14000.00, 'Expense', 'July Rent', '2024-07-03'),
(1, 2, 5, 2700.00, 'Expense', 'Weekly Groceries', '2024-07-05'),
(1, 2, 7, 1850.00, 'Expense', 'Electricity Bill', '2024-07-08'),
(1, 2, 6, 800.00, 'Expense', 'Fuel for bike', '2024-07-10'),
(1, 2, 10, 650.00, 'Expense', 'Gaming subscription', '2024-07-15'),
(1, 2, 5, 1300.00, 'Expense', 'Restaurant Dinner', '2024-07-18'),
(1, 4, 9, 3200.00, 'Expense', 'Summer Clothes', '2024-07-22');

-- August 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-08-01'),
(1, 2, 5, 2600.00, 'Expense', 'Weekly Groceries', '2024-08-04'),
(1, 2, 6, 900.00, 'Expense', 'Metro Card', '2024-08-09'),
(1, 1, 5, 1100.00, 'Expense', 'Cafe bill', '2024-08-14'),
(1, 2, 11, 1000.00, 'Expense', 'Dental checkup', '2024-08-19'),
(1, 2, 13, 2000.00, 'Expense', 'Personal Loan EMI', '2024-08-28');


-- September 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-09-01'),
(1, 2, 8, 14000.00, 'Expense', 'Sep Rent', '2024-09-03'),
(1, 2, 5, 2900.00, 'Expense', 'Weekly Groceries', '2024-09-07'),
(1, 2, 7, 1950.00, 'Expense', 'Internet Renewal', '2024-09-12'),
(1, 2, 6, 1000.00, 'Expense', 'Taxi for outing', '2024-09-16');

-- October 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-10-01'),
(1, 2, 5, 2500.00, 'Expense', 'Weekly Groceries', '2024-10-04'),
(1, 2, 6, 800.00, 'Expense', 'Fuel', '2024-10-08'),
(1, 4, 9, 2800.00, 'Expense', 'Festival Shopping', '2024-10-12'),
(1, 1, 10, 700.00, 'Expense', 'Diwali Fair Tickets', '2024-10-17'),
(1, 3, 4, 6000.00, 'Income', 'Investment Returns', '2024-10-25');

-- November 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-11-01'),
(1, 2, 8, 14000.00, 'Expense', 'Nov Rent', '2024-11-03'),
(1, 2, 5, 2600.00, 'Expense', 'Weekly Groceries', '2024-11-06'),
(1, 2, 7, 1700.00, 'Expense', 'Gas Bill', '2024-11-09'),
(1, 2, 6, 950.00, 'Expense', 'Taxi to relatives', '2024-11-13');

-- December 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 48000.00, 'Income', 'Monthly Salary', '2024-12-01'),
(1, 2, 5, 3000.00, 'Expense', 'Holiday Groceries', '2024-12-05'),
(1, 2, 6, 1100.00, 'Expense', 'Travel to native', '2024-12-10'),
(1, 4, 9, 6000.00, 'Expense', 'Christmas Gifts', '2024-12-15'),
(1, 1, 10, 1500.00, 'Expense', 'New Year Party', '2024-12-28');

-- Year 2025 (predictions will start from here)
-- January 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 49000.00, 'Income', 'Monthly Salary', '2025-01-01'),
(1, 2, 8, 14500.00, 'Expense', 'Jan Rent', '2025-01-03'),
(1, 2, 5, 2700.00, 'Expense', 'Weekly Groceries', '2025-01-05'),
(1, 2, 7, 1800.00, 'Expense', 'Electricity Bill', '2025-01-08'),
(1, 2, 6, 700.00, 'Expense', 'Local Bus Fare', '2025-01-10'),
(1, 2, 10, 600.00, 'Expense', 'Netflix Subscription', '2025-01-15');

-- February 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 49000.00, 'Income', 'Monthly Salary', '2025-02-01'),
(1, 2, 5, 2200.00, 'Expense', 'Weekly Groceries', '2025-02-04'),
(1, 2, 6, 850.00, 'Expense', 'Train Tickets', '2025-02-09'),
(1, 1, 5, 1000.00, 'Expense', 'Cafe Lunch', '2025-02-14'),
(1, 2, 11, 1500.00, 'Expense', 'Doctor Visit', '2025-02-19'),
(1, 2, 13, 2000.00, 'Expense', 'Personal Loan EMI', '2025-02-28');

-- March 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 49000.00, 'Income', 'Monthly Salary', '2025-03-01'),
(1, 2, 8, 14500.00, 'Expense', 'March Rent', '2025-03-03'),
(1, 2, 5, 2600.00, 'Expense', 'Weekly Groceries', '2025-03-07'),
(1, 2, 7, 1500.00, 'Expense', 'Water Bill', '2025-03-12'),
(1, 2, 6, 900.00, 'Expense', 'Cab Fares', '2025-03-16');

-- April 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 49000.00, 'Income', 'Monthly Salary', '2025-04-01'),
(1, 2, 5, 2400.00, 'Expense', 'Weekly Groceries', '2025-04-04'),
(1, 2, 6, 750.00, 'Expense', 'Fuel', '2025-04-08'),
(1, 4, 9, 2500.00, 'Expense', 'Books', '2025-04-12'),
(1, 1, 10, 500.00, 'Expense', 'Movie Tickets', '2025-04-17');

-- May 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 49000.00, 'Income', 'Monthly Salary', '2025-05-01'),
(1, 2, 8, 14500.00, 'Expense', 'May Rent', '2025-05-03'),
(1, 2, 5, 2700.00, 'Expense', 'Weekly Groceries', '2025-05-06'),
(1, 2, 7, 1900.00, 'Expense', 'Internet Bill', '2025-05-09'),
(1, 2, 6, 1000.00, 'Expense', 'Cab to Airport', '2025-05-13');

-- June 2025 (Current Month for testing context, today is mid-June)
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(1, 2, 1, 49000.00, 'Income', 'Monthly Salary', '2025-06-01'),
(1, 2, 5, 2800.00, 'Expense', 'Weekly Groceries', '2025-06-05'),
(1, 2, 6, 950.00, 'Expense', 'Local Travel', '2025-06-10'),
(1, 4, 9, 3500.00, 'Expense', 'New Gadget', '2025-06-15');


-- Insert Dummy Budgets for Ayushmaan (user_id = 1) (for current month and a past month)
-- User ID = 1
-- Category IDs: Food & Groceries=5, Transport=6, Utilities=7, Rent=8, Entertainment=10, Shopping=9
INSERT INTO budgets (user_id, category_id, amount_limit, month, year) VALUES
(1, 5, 5000.00, 6, 2025),
(1, 6, 2000.00, 6, 2025),
(1, 7, 2000.00, 6, 2025),
(1, 8, 14500.00, 6, 2025),
(1, 10, 1000.00, 6, 2025),
(1, 9, 4000.00, 6, 2025),
(1, 5, 4800.00, 5, 2025); -- Past budget


-- Calculate and update current_balance for all accounts based on initial_balance and all transactions
-- This should be run AFTER all transactions are inserted
-- User ID = 1
-- Account IDs: Cash=1, Main Checking=2, Savings=3, Credit Card=4

-- For Checking, Savings, Cash (Income adds, Expense subtracts)
UPDATE accounts acc
SET current_balance = (
    acc.initial_balance +
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Income'), 0.00) -
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Expense'), 0.00)
)
WHERE acc.user_id = 1 AND acc.account_type IN ('Checking', 'Savings', 'Cash');

-- For Credit Cards (Income reduces debt, Expense increases debt)
-- If initial_balance is negative (debt), expenses make it more negative, income makes it less negative
UPDATE accounts acc
SET current_balance = (
    acc.initial_balance +
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Expense'), 0.00) -
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Income'), 0.00)
)
WHERE acc.user_id = 1 AND acc.account_type = 'Credit Card';
