package repository;

import model.BankAccount;
import model.Customer;
import model.Loan;
import service.AuditService;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCLoanRepository implements LoanRepository{
    private double interestRate = 15.0;

    private Connection getConnection() throws SQLException {
        String url = DbConstants.DB_CONNECTION_URL;
        String user = DbConstants.DB_USER;
        String password = DbConstants.DB_PASSWORD;
        return DriverManager.getConnection(url, user, password);
    }
    public void addLoan(String customerName, double loanAmount, int repaymentPeriod) {
        Connection conn = null;
        PreparedStatement customerStmt = null;
        PreparedStatement loanStmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String insertCustomerSQL = "INSERT INTO customers(first_name) VALUES (?) RETURNING customer_id;";
            String insertLoanSQL = "INSERT INTO loans(amount, interest_rate, repayment_period, balance, customer_id) VALUES (?, ?, ?, ?, ?) RETURNING loan_id;";

            customerStmt = conn.prepareStatement(insertCustomerSQL, Statement.RETURN_GENERATED_KEYS);
            loanStmt = conn.prepareStatement(insertLoanSQL, Statement.RETURN_GENERATED_KEYS);

            customerStmt.setString(1, customerName);
            customerStmt.executeUpdate();

            rs = customerStmt.getGeneratedKeys();
            if (rs.next()) {
                int customerId = rs.getInt(1);
                loanStmt.setDouble(1, loanAmount);
                loanStmt.setDouble(2, interestRate);
                loanStmt.setInt(3, repaymentPeriod);
                loanStmt.setDouble(4, 0);
                loanStmt.setInt(5, customerId);
                loanStmt.executeUpdate();
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
                if (loanStmt != null) loanStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        AuditService.getInstance().logAction("Created loan");
    }

    public Map<Customer, List<Loan>> getLoans() throws SQLException {
        Map<Customer, List<Loan>> customers = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String query = "SELECT c.customer_id, c.first_name, l.account_id, l.balance FROM customers c " +
                    "JOIN loans l ON c.customer_id = l.customer_id";
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                String firstName = rs.getString("first_name");
                int loanId = rs.getInt("account_id");
                double loanAmount = rs.getDouble("amount");
                int repaymentPeriod = rs.getInt("repayment_period");

                Customer customer = new Customer(firstName);
                Loan loan = new Loan(loanId, loanAmount, interestRate, repaymentPeriod);

                customers.putIfAbsent(customer, new ArrayList<>());
                customers.get(customer).add(loan);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        AuditService.getInstance().logAction("Got customers with their loans");
        return customers;
    }
}
