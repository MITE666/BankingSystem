package repository;

import model.Customer;
import model.Loan;
import service.AuditService;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCLoanRepository implements LoanRepository {
    private double interestRate = 0.15;

    private Connection getConnection() throws SQLException {
        String url = DbConstants.DB_CONNECTION_URL;
        String user = DbConstants.DB_USER;
        String password = DbConstants.DB_PASSWORD;
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void addLoan(String customerName, double loanAmount, int repaymentPeriod) {
        Connection conn = null;
        PreparedStatement customerStmt = null;
        PreparedStatement loanStmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String insertCustomerSQL = "INSERT INTO customers(first_name) VALUES (?)";
            String insertLoanSQL = "INSERT INTO loans(amount, interest_rate, repayment_period, balance, customer_id) VALUES (?, ?, ?, ?, ?)";

            customerStmt = conn.prepareStatement(insertCustomerSQL, Statement.RETURN_GENERATED_KEYS);
            customerStmt.setString(1, customerName);
            customerStmt.executeUpdate();

            rs = customerStmt.getGeneratedKeys();
            if (rs.next()) {
                int customerId = rs.getInt(1);
                loanStmt = conn.prepareStatement(insertLoanSQL, Statement.RETURN_GENERATED_KEYS);
                loanStmt.setDouble(1, loanAmount);
                loanStmt.setDouble(2, interestRate);
                loanStmt.setInt(3, repaymentPeriod);
                loanStmt.setDouble(4, loanAmount);
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

    @Override
    public Map<Customer, List<Loan>> getLoans() throws SQLException {
        Map<Customer, List<Loan>> customers = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String query = "SELECT c.customer_id, c.first_name, l.loan_id, l.amount, l.repayment_period, l.balance FROM customers c " +
                    "JOIN loans l ON c.customer_id = l.customer_id";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String firstName = rs.getString("first_name");
                int loanId = rs.getInt("loan_id");
                double loanAmount = rs.getDouble("amount");
                int repaymentPeriod = rs.getInt("repayment_period");
                double balance = rs.getDouble("balance");

                Customer customer = new Customer(firstName);
                Loan loan = new Loan(loanId, loanAmount, interestRate, repaymentPeriod);
                loan.setCurrentBalance(balance);

                customers.putIfAbsent(customer, new ArrayList<>());
                customers.get(customer).add(loan);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Got customers with their loans");
        return customers;
    }

    public void makeLoanPayment(int loanId, double paymentAmount) throws SQLException {
        Connection conn = null;
        PreparedStatement checkLoanStmt = null;
        PreparedStatement updateLoanStmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            String checkLoanSQL = "SELECT balance FROM loans WHERE loan_id = ?";
            checkLoanStmt = conn.prepareStatement(checkLoanSQL);
            checkLoanStmt.setInt(1, loanId);
            rs = checkLoanStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (currentBalance < paymentAmount) {
                    throw new SQLException("Payment amount exceeds the remaining loan balance.");
                }
                String updateLoanSQL = "UPDATE loans SET balance = balance - ? WHERE loan_id = ?";
                updateLoanStmt = conn.prepareStatement(updateLoanSQL);
                updateLoanStmt.setDouble(1, paymentAmount);
                updateLoanStmt.setInt(2, loanId);
                int affectedRows = updateLoanStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Updating loan balance failed, no rows affected.");
                }
                conn.commit();
            } else {
                throw new SQLException("Loan not found.");
            }

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
            if (checkLoanStmt != null) checkLoanStmt.close();
            if (updateLoanStmt != null) updateLoanStmt.close();
            if (conn != null) conn.close();
        }
        AuditService.getInstance().logAction("Made loan payment");
    }
}
