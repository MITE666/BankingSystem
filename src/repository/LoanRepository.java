package repository;

import model.Customer;
import model.Loan;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface LoanRepository {
    void addLoan(String customerName, double loanAmount, int repaymentPeriod);

    Map<Customer, List<Loan>> getLoans() throws SQLException;

    public void makeLoanPayment(int loanId, double paymentAmount) throws SQLException;
}
