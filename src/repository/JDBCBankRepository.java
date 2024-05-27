package repository;

import model.*;
import service.AuditService;

import java.sql.*;
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
        BankAccount account = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String selectAccountSQL = "SELECT a.account_id, a.balance FROM customers c JOIN accounts a ON c.customer_id = a.customer_id WHERE c.first_name = ? AND a.account_id = ?";
            accountStatement = conn.prepareStatement(selectAccountSQL);
            accountStatement.setString(1, customer.getName());
            accountStatement.setInt(2, accountNumber);
            rs = accountStatement.executeQuery();
            if (rs.next()) {
                account = new BankAccount(rs.getInt("account_id"));
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
}
