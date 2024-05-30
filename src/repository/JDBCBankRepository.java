package repository;

import model.*;
import service.AuditService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCBankRepository implements BankRepository {
    private static double interestRate = 2.5;
    private static double mmInterestRate = 7.5;
    private static double withdrawalLimit = 5000;
    private static double overdraftLimit = 1000;
    private static int accountCount = 0;
    private Connection getConnection() throws SQLException {
        String url = DbConstants.DB_CONNECTION_URL;
        String user = DbConstants.DB_USER;
        String password = DbConstants.DB_PASSWORD;
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void createAccount(String customerName, double initialBalance, String accountType) {
        Connection conn = null;
        PreparedStatement customerStmt = null;
        PreparedStatement accountStmt = null;
        PreparedStatement subAccountStmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert customer
            String insertCustomerSQL = "INSERT INTO customers(first_name) VALUES (?)";
            customerStmt = conn.prepareStatement(insertCustomerSQL, Statement.RETURN_GENERATED_KEYS);
            customerStmt.setString(1, customerName);
            customerStmt.executeUpdate();

            rs = customerStmt.getGeneratedKeys();
            int customerId = 0;
            if (rs.next()) {
                customerId = rs.getInt(1);
            }

            // Insert account
            String insertAccountSQL = "INSERT INTO accounts(balance, customer_id) VALUES (?, ?)";
            accountStmt = conn.prepareStatement(insertAccountSQL, Statement.RETURN_GENERATED_KEYS);
            accountStmt.setDouble(1, initialBalance);
            accountStmt.setInt(2, customerId);
            accountStmt.executeUpdate();

            rs = accountStmt.getGeneratedKeys();
            int accountId = 0;
            if (rs.next()) {
                accountId = rs.getInt(1);
            }

            // Insert into specific account type table
            String insertSubAccountSQL;
            switch (accountType) {
                case "savings":
                    insertSubAccountSQL = "INSERT INTO savings_accounts(account_id, interest_rate) VALUES (?, ?)";
                    subAccountStmt = conn.prepareStatement(insertSubAccountSQL);
                    subAccountStmt.setInt(1, accountId);
                    subAccountStmt.setDouble(2, interestRate);
                    break;
                case "checking":
                    insertSubAccountSQL = "INSERT INTO checking_accounts(account_id, overdraft_limit) VALUES (?, ?)";
                    subAccountStmt = conn.prepareStatement(insertSubAccountSQL);
                    subAccountStmt.setInt(1, accountId);
                    subAccountStmt.setDouble(2, overdraftLimit);
                    break;
                case "MM":
                    insertSubAccountSQL = "INSERT INTO mm_accounts(account_id, interest_rate, withdrawal_limit) VALUES (?, ?, ?)";
                    subAccountStmt = conn.prepareStatement(insertSubAccountSQL);
                    subAccountStmt.setInt(1, accountId);
                    subAccountStmt.setDouble(2, mmInterestRate);
                    subAccountStmt.setDouble(3, withdrawalLimit);
                    break;
                default:
                    insertSubAccountSQL = "INSERT INTO cod_accounts(account_id, interest_rate, term_months) VALUES (?, ?, ?)";
                    subAccountStmt = conn.prepareStatement(insertSubAccountSQL);
                    subAccountStmt.setInt(1, accountId);
                    subAccountStmt.setDouble(2, interestRate);
                    subAccountStmt.setInt(3, 12); // Assuming a default term of 12 months for COD accounts
                    break;
            }
            if (subAccountStmt != null) {
                subAccountStmt.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (customerStmt != null) customerStmt.close();
                if (accountStmt != null) accountStmt.close();
                if (subAccountStmt != null) subAccountStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        AuditService.getInstance().logAction("Created account");
    }

    public Customer getCustomer(int accountNumber) throws SQLException {
        Connection conn = null;
        PreparedStatement customerStatement = null;
        ResultSet rs = null;
        Customer customer = null;
        try {
            conn = getConnection();
            String selectCustomerSQL = "SELECT c.first_name FROM customers c JOIN accounts a ON c.customer_id = a.customer_id WHERE a.account_id = ?";
            customerStatement = conn.prepareStatement(selectCustomerSQL);
            customerStatement.setInt(1, accountNumber);
            rs = customerStatement.executeQuery();
            if (rs.next()) {
                customer = new Customer(rs.getString("first_name"));
            }
        } finally {
            if (rs != null) rs.close();
            if (customerStatement != null) customerStatement.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Got customer");
        return customer;
    }

    public BankAccount getAccount(Customer customer, int accountNumber) throws SQLException {
        Connection conn = null;
        PreparedStatement accountStatement = null;
        ResultSet rs = null;
        BankAccount account = null;
        try {
            conn = getConnection();
            String selectAccountSQL = "SELECT a.account_id, a.balance, " +
                    "sa.interest_rate AS sa_interest_rate, " +
                    "ca.overdraft_limit AS ca_overdraft_limit, " +
                    "mma.interest_rate AS mma_interest_rate, mma.withdrawal_limit, " +
                    "coda.interest_rate AS coda_interest_rate, coda.term_months " +
                    "FROM accounts a " +
                    "LEFT JOIN savings_accounts sa ON a.account_id = sa.account_id " +
                    "LEFT JOIN checking_accounts ca ON a.account_id = ca.account_id " +
                    "LEFT JOIN mm_accounts mma ON a.account_id = mma.account_id " +
                    "LEFT JOIN cod_accounts coda ON a.account_id = coda.account_id " +
                    "JOIN customers c ON a.customer_id = c.customer_id " +
                    "WHERE c.first_name = ? AND a.account_id = ?";
            accountStatement = conn.prepareStatement(selectAccountSQL);
            accountStatement.setString(1, customer.getName());
            accountStatement.setInt(2, accountNumber);
            rs = accountStatement.executeQuery();
            if (rs.next()) {
                int accountId = rs.getInt("account_id");
                double balance = rs.getDouble("balance");
                if (rs.getString("sa_interest_rate") != null) {
                    double interestRate = rs.getDouble("sa_interest_rate");
                    account = new SavingsAccount(accountId, interestRate);
                } else if (rs.getString("ca_overdraft_limit") != null) {
                    double overdraftLimit = rs.getDouble("ca_overdraft_limit");
                    account = new CheckingAccount(accountId, overdraftLimit);
                } else if (rs.getString("mma_interest_rate") != null) {
                    double interestRate = rs.getDouble("mma_interest_rate");
                    double withdrawalLimit = rs.getDouble("withdrawal_limit");
                    account = new MoneyMarketAccount(accountId, interestRate, withdrawalLimit);
                } else if (rs.getString("coda_interest_rate") != null) {
                    double interestRate = rs.getDouble("coda_interest_rate");
                    int termMonths = rs.getInt("term_months");
                    account = new CertificateOfDepositAccount(accountId, interestRate, termMonths);
                } else {
                    account = new BankAccount(accountId);
                }
                account.setBalance(balance);
            }
        } finally {
            if (rs != null) rs.close();
            if (accountStatement != null) accountStatement.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Got account");
        return account;
    }


    public List<Transaction> getTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String selectTransactionSQL = "SELECT * FROM transactions";
            stmt = conn.prepareStatement(selectTransactionSQL);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getString("descr"),
                        rs.getDouble("amount"),
                        new BankAccount(rs.getInt("source_id")),
                        new BankAccount(rs.getInt("destination_id"))
                );
                transactions.add(transaction);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Got transactions");
        return transactions;
    }

    public Map<Customer, List<BankAccount>> getCustomers() throws SQLException {
        Map<Customer, List<BankAccount>> customers = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String query = "SELECT c.customer_id, c.first_name, a.account_id, a.balance FROM customers c " +
                    "JOIN accounts a ON c.customer_id = a.customer_id";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String firstName = rs.getString("first_name");
                int accountId = rs.getInt("account_id");
                double balance = rs.getDouble("balance");

                Customer customer = new Customer(firstName);
                BankAccount bankAccount = new BankAccount(accountId);
                bankAccount.setBalance(balance);

                customers.putIfAbsent(customer, new ArrayList<>());
                customers.get(customer).add(bankAccount);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Got customers with their accounts");
        return customers;
    }

    public void addTransaction(Transaction transaction) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String insertTransactionSQL = "INSERT INTO transactions (t_date, descr, amount, source_id, destination_id) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setString(2, transaction.getDescription());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setInt(4, transaction.getSource().getAccountNumber());
            stmt.setInt(5, transaction.getDestination().getAccountNumber());
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int transactionId = rs.getInt(1);
                System.out.println("Transaction ID: " + transactionId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        AuditService.getInstance().logAction("Added transaction: " + transaction.getDescription());
    }

    public void addDeposit(int accountNumber, double amount) throws SQLException {
        Connection conn = null;
        PreparedStatement updateBalanceStmt = null;
        PreparedStatement insertTransactionStmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Update account balance
            String updateBalanceSQL = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
            updateBalanceStmt = conn.prepareStatement(updateBalanceSQL);
            updateBalanceStmt.setDouble(1, amount);
            updateBalanceStmt.setInt(2, accountNumber);
            int affectedRows = updateBalanceStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating balance failed, no rows affected.");
            }

            // Insert transaction record
            String insertTransactionSQL = "INSERT INTO transactions (t_date, descr, amount, source_id, destination_id) VALUES (?, ?, ?, ?, ?)";
            insertTransactionStmt = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS);
            insertTransactionStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            insertTransactionStmt.setString(2, "DEPOSIT");
            insertTransactionStmt.setDouble(3, amount);
            insertTransactionStmt.setInt(4, accountNumber);
            insertTransactionStmt.setInt(5, accountNumber);
            insertTransactionStmt.executeUpdate();

            // Commit transaction
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (updateBalanceStmt != null) updateBalanceStmt.close();
            if (insertTransactionStmt != null) insertTransactionStmt.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Added deposit");
    }

    public void withdraw(int accountNumber, double amount) throws SQLException {
        Connection conn = null;
        PreparedStatement checkBalanceStmt = null;
        PreparedStatement updateBalanceStmt = null;
        PreparedStatement insertTransactionStmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Check account balance
            String checkBalanceSQL = "SELECT balance FROM accounts WHERE account_id = ?";
            checkBalanceStmt = conn.prepareStatement(checkBalanceSQL);
            checkBalanceStmt.setInt(1, accountNumber);
            rs = checkBalanceStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (currentBalance < amount) {
                    throw new SQLException("Insufficient funds for withdrawal.");
                }
            } else {
                throw new SQLException("Account not found.");
            }

            // Update account balance
            String updateBalanceSQL = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
            updateBalanceStmt = conn.prepareStatement(updateBalanceSQL);
            updateBalanceStmt.setDouble(1, amount);
            updateBalanceStmt.setInt(2, accountNumber);
            int affectedRows = updateBalanceStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating balance failed, no rows affected.");
            }

            // Insert transaction record
            String insertTransactionSQL = "INSERT INTO transactions (t_date, descr, amount, source_id, destination_id) VALUES (?, ?, ?, ?, ?)";
            insertTransactionStmt = conn.prepareStatement(insertTransactionSQL, Statement.RETURN_GENERATED_KEYS);
            insertTransactionStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            insertTransactionStmt.setString(2, "WITHDRAWAL");
            insertTransactionStmt.setDouble(3, -amount);
            insertTransactionStmt.setInt(4, accountNumber);
            insertTransactionStmt.setInt(5, accountNumber);
            insertTransactionStmt.executeUpdate();

            // Commit transaction
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (rs != null) rs.close();
            if (checkBalanceStmt != null) checkBalanceStmt.close();
            if (updateBalanceStmt != null) updateBalanceStmt.close();
            if (insertTransactionStmt != null) insertTransactionStmt.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Withdrawal");
    }

}
