package repository;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
import model.BankAccount;
import model.Customer;
import model.Transaction;
import service.AuditService;

import java.lang.reflect.AccessFlag;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCBankRepository implements BankRepository {
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
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String insertCustomerSQL = "INSERT INTO customers(first_name) VALUES (?) RETURNING customer_id;";
            String insertAccountSQL = "INSERT INTO accounts(balance, customer_id) VALUES (?, ?) RETURNING account_id;";

            customerStmt = conn.prepareStatement(insertCustomerSQL, Statement.RETURN_GENERATED_KEYS);
            accountStmt = conn.prepareStatement(insertAccountSQL, Statement.RETURN_GENERATED_KEYS);

            customerStmt.setString(1, customerName);
            customerStmt.executeUpdate();

            rs = customerStmt.getGeneratedKeys();
            if (rs.next()) {
                int customerId = rs.getInt(1);
                accountStmt.setDouble(1, initialBalance);
                accountStmt.setInt(2, customerId);
                accountStmt.executeUpdate();
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
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        AuditService.getInstance().logAction("Created account");
    }

    public Customer getCustomer(int accountNumber) throws SQLException {
        PreparedStatement customerStatement = null;
        ResultSet rs = null;
        try {
            String selectCustomerSQL = "SELECT c FROM customers c JOIN accounts a ON c.customer_id = a.customer_id WHERE a.account_id = ?;";
            customerStatement.setInt(1, accountNumber);
            rs = customerStatement.executeQuery(selectCustomerSQL);
            while (rs.next()) {
                Customer customer = new Customer(rs.getString("first_name"));
                return customer;
            }
        } finally {
            if (rs != null) rs.close();
            if (customerStatement != null) customerStatement.close();
        }
        AuditService.getInstance().logAction("Got customer");
        return null;
    }

    public BankAccount getAccount(Customer customer, int accountNumber) throws SQLException {
        PreparedStatement accountStatement = null;
        BankAccount account = null;
        ResultSet rs = null;
        try {
            String selectAccountSQL = "SELECT a FROM customers c JOIN accounts a ON c.customer_id = a.customer_id AND c.first_name = ? WHERE a.account_id = ?;";
            accountStatement.setString(1, customer.getName());
            accountStatement.setInt(2, accountNumber);
            rs = accountStatement.executeQuery(selectAccountSQL);
            while (rs.next()) {
                account = new BankAccount(rs.getInt("account_id"));
            }
        } finally {
            if (rs != null) rs.close();
            if (accountStatement != null) accountStatement.close();
        }
        AuditService.getInstance().logAction("Got account");
        return account;
    }

    public List<Transaction> getTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String selectTransactionSQL = "SELECT * FROM transactions;";
            rs = stmt.executeQuery(selectTransactionSQL);
            while (rs.next()) {
                Transaction transaction = new Transaction(rs.getString("descr"), rs.getDouble("amount"), new BankAccount(rs.getInt("source_id")), new BankAccount(rs.getInt("destintaion_id")));
                transactions.add(transaction);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        AuditService.getInstance().logAction("Got transactions");
        return transactions;
    }

    public Map<Customer, List<BankAccount>> getCustomers() throws SQLException {
        Map<Customer, List<BankAccount>> customers = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
                String query = "SELECT c.customer_id, c.first_name, a.account_id, a.balance FROM customers c " +
                        "JOIN accounts a ON c.customer_id = a.customer_id";
                rs = stmt.executeQuery(query);

                while (rs.next()) {
                    String firstName = rs.getString("first_name");
                    int accountId = rs.getInt("account_id");

                    Customer customer = new Customer(firstName);
                    BankAccount bankAccount = new BankAccount(accountId);

                    customers.putIfAbsent(customer, new ArrayList<>());
                    customers.get(customer).add(bankAccount);
                }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        AuditService.getInstance().logAction("Got customers with their accounts");
        return customers;
    }
}
