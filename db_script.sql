CREATE TABLE customers (
                           customer_id INT PRIMARY KEY AUTO_INCREMENT,
                           first_name VARCHAR(50)
);

CREATE TABLE loans (
                       loan_id INT PRIMARY KEY AUTO_INCREMENT,
                       amount DECIMAL(10, 2),
                       interest_rate DECIMAL(4, 3),
                       repayment_period INT,
                       balance DECIMAL(10, 2),
                       customer_id INT,
                       FOREIGN KEY(customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE accounts (
                          account_id INT PRIMARY KEY AUTO_INCREMENT,
                          balance DECIMAL(10, 2),
                          customer_id INT,
                          FOREIGN KEY(customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE transactions (
                              transaction_id INT PRIMARY KEY AUTO_INCREMENT,
                              t_date DATE,
                              descr VARCHAR(100),
                              amount DECIMAL(10, 2),
                              source_id INT,
                              destination_id INT,
                              FOREIGN KEY(source_id) REFERENCES customers(customer_id),
                              FOREIGN KEY(destination_id) REFERENCES customers(customer_id)
);

CREATE TABLE cod_accounts (
                              account_id INT PRIMARY KEY,
                              interest_rate DECIMAL(4, 3),
                              term_months INT,
                              FOREIGN KEY(account_id) REFERENCES accounts(account_id)
);

CREATE TABLE checking_accounts (
                                   account_id INT PRIMARY KEY,
                                   overdraft_limit DECIMAL(10, 2),
                                   FOREIGN KEY(account_id) REFERENCES accounts(account_id)
);

CREATE TABLE mm_accounts (
                             account_id INT PRIMARY KEY,
                             interest_rate DECIMAL(4, 3),
                             withdrawal_limit DECIMAL(10, 2),
                             FOREIGN KEY(account_id) REFERENCES accounts(account_id)
);

CREATE TABLE savings_accounts (
                                  account_id INT PRIMARY KEY,
                                  interest_rate DECIMAL(4, 3),
                                  FOREIGN KEY(account_id) REFERENCES accounts(account_id)
);