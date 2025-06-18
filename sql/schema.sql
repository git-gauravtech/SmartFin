-- Create the database if it doesn't exist
-- CREATE DATABASE IF NOT EXISTS finance_app_db;

-- Use the created database
USE finance_app_db;

-- Table for Users
-- CREATE TABLE IF NOT EXISTS users (
--    user_id INT AUTO_INCREMENT PRIMARY KEY,
--    username VARCHAR(100) UNIQUE NOT NULL,
--    password_hash VARCHAR(255) NOT NULL, -- Storing hashed passwords
--    password_salt VARCHAR(255) NOT NULL, -- Storing salt used for hashing
--    is_admin BOOLEAN DEFAULT FALSE,     -- Flag to identify admin users
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

-- Table for Accounts
-- Each user can have multiple accounts (e.g., Savings, Checking, Credit Card)
-- CREATE TABLE IF NOT EXISTS accounts (
--    account_id INT AUTO_INCREMENT PRIMARY KEY,
--    user_id INT NOT NULL,
--    account_name VARCHAR(100) NOT NULL,
--    initial_balance DECIMAL(10, 2) DEFAULT 0.00, -- Initial balance of the account
--    current_balance DECIMAL(10, 2) DEFAULT 0.00, -- Dynamic current balance (calculated by app logic, or derived from initial + transactions for dummy data)
--    account_type VARCHAR(50), -- e.g., 'Checking', 'Savings', 'Credit Card', 'Cash', 'Investment', 'Loan'
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
--    UNIQUE (user_id, account_name) -- A user cannot have two accounts with the same name
-- );

-- Table for Categories
-- Users can define their own categories
-- CREATE TABLE IF NOT EXISTS categories (
--    category_id INT AUTO_INCREMENT PRIMARY KEY,
--    user_id INT NOT NULL, -- User who owns this custom category
--    category_name VARCHAR(100) NOT NULL,
--    category_type ENUM('Income', 'Expense') NOT NULL, -- Is this an income or expense category?
--    is_default BOOLEAN DEFAULT FALSE, -- Flag for system-provided default categories
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
--    UNIQUE (user_id, category_name) -- A user cannot have two categories with the same name
-- );

-- Table for Transactions (MODIFIED)
-- Now includes account_id to link transactions to specific accounts
-- The category_id will link to the new categories table
-- CREATE TABLE IF NOT EXISTS transactions (
--    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
--    user_id INT NOT NULL,
--    account_id INT NOT NULL, -- New: Link to account
--    category_id INT NOT NULL, -- New: Link to category
--    amount DECIMAL(10, 2) NOT NULL,
--    type ENUM('Income', 'Expense') NOT NULL, -- Redundant with category_type, but kept for clarity/compatibility
--    description TEXT,
--    transaction_date DATE NOT NULL,
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
--    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
--    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
-- );

-- Table for Budgets (MODIFIED)
-- Now links to category_id instead of category_name string
-- CREATE TABLE IF NOT EXISTS budgets (
--    budget_id INT AUTO_INCREMENT PRIMARY KEY,
--    user_id INT NOT NULL,
--    category_id INT NOT NULL, -- New: Link to category
--    amount_limit DECIMAL(10, 2) NOT NULL,
--    month INT NOT NULL, -- Month (1-12)
--    year INT NOT NULL,  -- Year (e.g., 2024)
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    UNIQUE (user_id, category_id, month, year), -- Ensures one budget per category per month/year per user
--    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
--    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
-- );

--
-- DUMMY DATA INSERTS BELOW
--

-- Insert Dummy Users
-- Passwords: 'ayushmaan1234' for ayushmaan, 'gaurav1234' for gaurav
-- INSERT INTO users (username, password_hash, password_salt, is_admin)
-- VALUES ('ayushmaan', 'pU0d1b11H/n/42jB2y0+8rK2tGg4g3i4o9v4s9e4q8w4i7f5g2j2o7p7q8u2w7y4a5f3s7e4d8c6b2n6m4j7g9h1k0l9m4n2b1v2c6x4z7e8d0f1h2j3k4l5m6n7b8v9c0x1z2e3d4f5g6h7j8k9l0m1n2b3v4c5x6z7', 'b0bN9s+cRjZfR2q/Xl+Mqw==', FALSE);
--
---- Admin user (no personal finance data beyond this user entry)
-- INSERT INTO users (username, password_hash, password_salt, is_admin)
-- VALUES ('gaurav', 'oT2p3o4p5q6r7s8t9u0v1w2x3y4z5a6b7c8d9e0f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0z1a2b3c4d5e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z', 'k2kS0l+XcYfM8p+Y2g+PqA==', TRUE);

-- Get the user_ids for Ayushmaan and Gaurav
SET @ayushmaan_id = (SELECT user_id FROM users WHERE username = 'Ayushmaan');
SET @gaurav_id = (SELECT user_id FROM users WHERE username = 'Gaurav Saklani');


-- Insert Default Accounts for Ayushmaan (initial balance will be updated after transactions)
-- INSERT INTO accounts (user_id, account_name, initial_balance, current_balance, account_type) VALUES
-- (@ayushmaan_id, 'Cash', 1000.00, 1000.00, 'Cash'),
-- (@ayushmaan_id, 'Main Checking', 15000.00, 15000.00, 'Checking'),
-- (@ayushmaan_id, 'Savings', 50000.00, 50000.00, 'Savings'),
-- (@ayushmaan_id, 'Credit Card', -5000.00, -5000.00, 'Credit Card'); -- Negative for credit card debt

-- Get the account_ids for dummy accounts for Ayushmaan
SET @ayushmaan_cash_id = (SELECT account_id FROM accounts WHERE user_id = @ayushmaan_id AND account_name = 'Cash');
SET @ayushmaan_checking_id = (SELECT account_id FROM accounts WHERE user_id = @ayushmaan_id AND account_name = 'Main Checking');
SET @ayushmaan_savings_id = (SELECT account_id FROM accounts WHERE user_id = @ayushmaan_id AND account_name = 'Savings');
SET @ayushmaan_credit_id = (SELECT account_id FROM accounts WHERE user_id = @ayushmaan_id AND account_name = 'Credit Card');


-- Insert Default Categories for Ayushmaan
-- INSERT IGNORE INTO categories (user_id, category_name, category_type, is_default) VALUES
-- (@ayushmaan_id, 'Salary', 'Income', TRUE),
-- (@ayushmaan_id, 'Freelance', 'Income', TRUE),
-- (@ayushmaan_id, 'Bonus', 'Income', FALSE),
-- (@ayushmaan_id, 'Investments Income', 'Income', FALSE),
-- (@ayushmaan_id, 'Food & Groceries', 'Expense', TRUE),
-- (@ayushmaan_id, 'Transport', 'Expense', TRUE),
-- (@ayushmaan_id, 'Utilities', 'Expense', TRUE),
-- (@ayushmaan_id, 'Rent', 'Expense', TRUE),
-- (@ayushmaan_id, 'Shopping', 'Expense', TRUE),
-- (@ayushmaan_id, 'Entertainment', 'Expense', TRUE),
-- (@ayushmaan_id, 'Healthcare', 'Expense', TRUE),
-- (@ayushmaan_id, 'Education', 'Expense', TRUE),
-- (@ayushmaan_id, 'Loan Repayment', 'Expense', FALSE),
-- (@ayushmaan_id, 'Miscellaneous', 'Expense', TRUE);

-- Get Category IDs for dummy data population for Ayushmaan
SET @cat_ayushmaan_salary = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Salary');
SET @cat_ayushmaan_freelance = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Freelance');
SET @cat_ayushmaan_bonus = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Bonus');
SET @cat_ayushmaan_investments = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Investments Income');
SET @cat_ayushmaan_food_groceries = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Food & Groceries');
SET @cat_ayushmaan_transport = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Transport');
SET @cat_ayushmaan_utilities = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Utilities');
SET @cat_ayushmaan_rent = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Rent');
SET @cat_ayushmaan_shopping = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Shopping');
SET @cat_ayushmaan_entertainment = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Entertainment');
SET @cat_ayushmaan_healthcare = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Healthcare');
SET @cat_ayushmaan_education = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Education');
SET @cat_ayushmaan_loan_repayment = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Loan Repayment');
SET @cat_ayushmaan_misc = (SELECT category_id FROM categories WHERE user_id = @ayushmaan_id AND category_name = 'Miscellaneous');


-- Insert Extensive & Realistic Dummy Transactions for Ayushmaan (Jan 2024 - June 2025)
-- Year 2024
-- January 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-01-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14000.00, 'Expense', 'Jan Rent', '2024-01-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2500.00, 'Expense', 'Weekly Groceries', '2024-01-05'),
(@ayushmaan_id, @ayushmaan_utilities, 1800.00, 'Expense', 'Electricity Bill', '2024-01-08'),
(@ayushmaan_id, @ayushmaan_transport, 700.00, 'Expense', 'Local Bus Fare', '2024-01-10'),
(@ayushmaan_id, @ayushmaan_entertainment, 600.00, 'Expense', 'Netflix Subscription', '2024-01-15'),
(@ayushmaan_id, @ayushmaan_food_groceries, 1200.00, 'Expense', 'Restaurant Dinner', '2024-01-18'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 3000.00, 'Expense', 'Winter Clothes', '2024-01-22'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_misc, 300.00, 'Expense', 'Misc Expenses', '2024-01-28'),
(@ayushmaan_id, @ayushmaan_savings_id, @cat_ayushmaan_investments, 5000.00, 'Income', 'Stock Dividends', '2024-01-30');


-- February 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-02-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 2200.00, 'Expense', 'Weekly Groceries', '2024-02-04'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 850.00, 'Expense', 'Train Tickets', '2024-02-09'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_food_groceries, 1000.00, 'Expense', 'Cafe Lunch', '2024-02-14'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_healthcare, 1500.00, 'Expense', 'Doctor Visit', '2024-02-19'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_entertainment, 700.00, 'Expense', 'Concert Tickets', '2024-02-23'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_loan_repayment, 2000.00, 'Expense', 'Personal Loan EMI', '2024-02-28');

-- March 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-03-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14000.00, 'Expense', 'March Rent', '2024-03-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2600.00, 'Expense', 'Weekly Groceries', '2024-03-07'),
(@ayushmaan_id, @ayushmaan_utilities, 1500.00, 'Expense', 'Water Bill', '2024-03-12'),
(@ayushmaan_id, @ayushmaan_transport, 900.00, 'Expense', 'Cab Fares', '2024-03-16'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_food_groceries, 1500.00, 'Expense', 'Weekend Brunch', '2024-03-20'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_education, 4000.00, 'Expense', 'Online Course Fee', '2024-03-25');

-- April 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-04-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 2400.00, 'Expense', 'Weekly Groceries', '2024-04-04'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 750.00, 'Expense', 'Fuel', '2024-04-08'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 2500.00, 'Expense', 'Books', '2024-04-12'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_entertainment, 500.00, 'Expense', 'Movie Tickets', '2024-04-17'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_misc, 200.00, 'Expense', 'Small Purchase', '2024-04-25'),
(@ayushmaan_id, @ayushmaan_savings_id, @cat_ayushmaan_bonus, 8000.00, 'Income', 'Performance Bonus', '2024-04-30');

-- May 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-05-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14000.00, 'Expense', 'May Rent', '2024-05-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2700.00, 'Expense', 'Weekly Groceries', '2024-05-06'),
(@ayushmaan_id, @ayushmaan_utilities, 1900.00, 'Expense', 'Internet Bill', '2024-05-09'),
(@ayushmaan_id, @ayushmaan_transport, 1000.00, 'Expense', 'Cab to Airport', '2024-05-13'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_food_groceries, 1800.00, 'Expense', 'Dinner with Family', '2024-05-16'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_healthcare, 800.00, 'Expense', 'Medicine Purchase', '2024-05-20');

-- June 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-06-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 2800.00, 'Expense', 'Weekly Groceries', '2024-06-05'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 950.00, 'Expense', 'Local Travel', '2024-06-10'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 3500.00, 'Expense', 'New Gadget', '2024-06-15'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_food_groceries, 1600.00, 'Expense', 'Dining out', '2024-06-18');


-- July 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-07-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14000.00, 'Expense', 'July Rent', '2024-07-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2700.00, 'Expense', 'Weekly Groceries', '2024-07-05'),
(@ayushmaan_id, @ayushmaan_utilities, 1850.00, 'Expense', 'Electricity Bill', '2024-07-08'),
(@ayushmaan_id, @ayushmaan_transport, 800.00, 'Expense', 'Fuel for bike', '2024-07-10'),
(@ayushmaan_id, @ayushmaan_entertainment, 650.00, 'Expense', 'Gaming subscription', '2024-07-15'),
(@ayushmaan_id, @ayushmaan_food_groceries, 1300.00, 'Expense', 'Restaurant Dinner', '2024-07-18'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 3200.00, 'Expense', 'Summer Clothes', '2024-07-22');

-- August 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-08-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 2600.00, 'Expense', 'Weekly Groceries', '2024-08-04'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 900.00, 'Expense', 'Metro Card', '2024-08-09'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_food_groceries, 1100.00, 'Expense', 'Cafe bill', '2024-08-14'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_healthcare, 1000.00, 'Expense', 'Dental checkup', '2024-08-19'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_loan_repayment, 2000.00, 'Expense', 'Personal Loan EMI', '2024-08-28');


-- September 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-09-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14000.00, 'Expense', 'Sep Rent', '2024-09-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2900.00, 'Expense', 'Weekly Groceries', '2024-09-07'),
(@ayushmaan_id, @ayushmaan_utilities, 1950.00, 'Expense', 'Internet Renewal', '2024-09-12'),
(@ayushmaan_id, @ayushmaan_transport, 1000.00, 'Expense', 'Taxi for outing', '2024-09-16');

-- October 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-10-01'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2500.00, 'Expense', 'Weekly Groceries', '2024-10-04'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 800.00, 'Expense', 'Fuel', '2024-10-08'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 2800.00, 'Expense', 'Festival Shopping', '2024-10-12'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_entertainment, 700.00, 'Expense', 'Diwali Fair Tickets', '2024-10-17'),
(@ayushmaan_id, @ayushmaan_savings_id, @cat_ayushmaan_investments, 6000.00, 'Income', 'Investment Returns', '2024-10-25');

-- November 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-11-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14000.00, 'Expense', 'Nov Rent', '2024-11-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2600.00, 'Expense', 'Weekly Groceries', '2024-11-06'),
(@ayushmaan_id, @ayushmaan_utilities, 1700.00, 'Expense', 'Gas Bill', '2024-11-09'),
(@ayushmaan_id, @ayushmaan_transport, 950.00, 'Expense', 'Taxi to relatives', '2024-11-13');

-- December 2024
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 48000.00, 'Income', 'Monthly Salary', '2024-12-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 3000.00, 'Expense', 'Holiday Groceries', '2024-12-05'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 1100.00, 'Expense', 'Travel to native', '2024-12-10'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 6000.00, 'Expense', 'Christmas Gifts', '2024-12-15'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_entertainment, 1500.00, 'Expense', 'New Year Party', '2024-12-28');

-- Year 2025 (predictions will start from here)
-- January 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 49000.00, 'Income', 'Monthly Salary', '2025-01-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14500.00, 'Expense', 'Jan Rent', '2025-01-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2700.00, 'Expense', 'Weekly Groceries', '2025-01-05'),
(@ayushmaan_id, @ayushmaan_utilities, 1800.00, 'Expense', 'Electricity Bill', '2025-01-08'),
(@ayushmaan_id, @ayushmaan_transport, 700.00, 'Expense', 'Local Bus Fare', '2025-01-10'),
(@ayushmaan_id, @ayushmaan_entertainment, 600.00, 'Expense', 'Netflix Subscription', '2025-01-15');

-- February 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 49000.00, 'Income', 'Monthly Salary', '2025-02-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 2200.00, 'Expense', 'Weekly Groceries', '2025-02-04'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 850.00, 'Expense', 'Train Tickets', '2025-02-09'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_food_groceries, 1000.00, 'Expense', 'Cafe Lunch', '2025-02-14'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_healthcare, 1500.00, 'Expense', 'Doctor Visit', '2025-02-19'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_loan_repayment, 2000.00, 'Expense', 'Personal Loan EMI', '2025-02-28');

-- March 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 49000.00, 'Income', 'Monthly Salary', '2025-03-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14500.00, 'Expense', 'March Rent', '2025-03-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2600.00, 'Expense', 'Weekly Groceries', '2025-03-07'),
(@ayushmaan_id, @ayushmaan_utilities, 1500.00, 'Expense', 'Water Bill', '2025-03-12'),
(@ayushmaan_id, @ayushmaan_transport, 900.00, 'Expense', 'Cab Fares', '2025-03-16');

-- April 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 49000.00, 'Income', 'Monthly Salary', '2025-04-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 2400.00, 'Expense', 'Weekly Groceries', '2025-04-04'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 750.00, 'Expense', 'Fuel', '2025-04-08'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 2500.00, 'Expense', 'Books', '2025-04-12'),
(@ayushmaan_id, @ayushmaan_cash_id, @cat_ayushmaan_entertainment, 500.00, 'Expense', 'Movie Tickets', '2025-04-17');

-- May 2025
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 49000.00, 'Income', 'Monthly Salary', '2025-05-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_rent, 14500.00, 'Expense', 'May Rent', '2025-05-03'),
(@ayushmaan_id, @ayushmaan_food_groceries, 2700.00, 'Expense', 'Weekly Groceries', '2025-05-06'),
(@ayushmaan_id, @ayushmaan_utilities, 1900.00, 'Expense', 'Internet Bill', '2025-05-09'),
(@ayushmaan_id, @ayushmaan_transport, 1000.00, 'Expense', 'Cab to Airport', '2025-05-13');

-- June 2025 (Current Month for testing context, today is mid-June)
INSERT INTO transactions (user_id, account_id, category_id, amount, type, description, transaction_date) VALUES
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_salary, 49000.00, 'Income', 'Monthly Salary', '2025-06-01'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_food_groceries, 2800.00, 'Expense', 'Weekly Groceries', '2025-06-05'),
(@ayushmaan_id, @ayushmaan_checking_id, @cat_ayushmaan_transport, 950.00, 'Expense', 'Local Travel', '2025-06-10'),
(@ayushmaan_id, @ayushmaan_credit_id, @cat_ayushmaan_shopping, 3500.00, 'Expense', 'New Gadget', '2025-06-15');


-- Insert Dummy Budgets for Ayushmaan (for current month and a past month)
INSERT INTO budgets (user_id, category_id, amount_limit, month, year) VALUES
(@ayushmaan_id, @cat_ayushmaan_food_groceries, 5000.00, 6, 2025),
(@ayushmaan_id, @cat_ayushmaan_transport, 2000.00, 6, 2025),
(@ayushmaan_id, @cat_ayushmaan_utilities, 2000.00, 6, 2025),
(@ayushmaan_id, @cat_ayushmaan_rent, 14500.00, 6, 2025),
(@ayushmaan_id, @cat_ayushmaan_entertainment, 1000.00, 6, 2025),
(@ayushmaan_id, @cat_ayushmaan_shopping, 4000.00, 6, 2025),
(@ayushmaan_id, @cat_ayushmaan_food_groceries, 4800.00, 5, 2025); -- Past budget


-- Calculate and update current_balance for all accounts based on initial_balance and all transactions
-- This should be run AFTER all transactions are inserted

-- For Checking, Savings, Cash (Income adds, Expense subtracts)
UPDATE accounts acc
SET current_balance = (
    acc.initial_balance +
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Income'), 0.00) -
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Expense'), 0.00)
)
WHERE acc.user_id = @ayushmaan_id AND acc.account_type IN ('Checking', 'Savings', 'Cash');

-- For Credit Cards (Income reduces debt, Expense increases debt)
-- If initial_balance is negative (debt), expenses make it more negative, income makes it less negative
UPDATE accounts acc
SET current_balance = (
    acc.initial_balance +
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Expense'), 0.00) -
    COALESCE((SELECT SUM(t.amount) FROM transactions t WHERE t.account_id = acc.account_id AND t.type = 'Income'), 0.00)
)
WHERE acc.user_id = @ayushmaan_id AND acc.account_type = 'Credit Card';

